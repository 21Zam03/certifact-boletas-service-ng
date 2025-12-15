package com.certicom.certifact_boletas_service_ng.service.impl;

import com.certicom.certifact_boletas_service_ng.dto.CompanyDto;
import com.certicom.certifact_boletas_service_ng.dto.RegisterFileUploadDto;
import com.certicom.certifact_boletas_service_ng.enums.LogTitle;
import com.certicom.certifact_boletas_service_ng.enums.TipoArchivoEnum;
import com.certicom.certifact_boletas_service_ng.exception.ServiceException;
import com.certicom.certifact_boletas_service_ng.feign.rest.RegisterFileUploadRestService;
import com.certicom.certifact_boletas_service_ng.feign.rest.SummaryDocumentsRestService;
import com.certicom.certifact_boletas_service_ng.service.AmazonS3ClientService;
import com.certicom.certifact_boletas_service_ng.util.LogHelper;
import com.certicom.certifact_boletas_service_ng.util.LogMessages;
import com.certicom.certifact_boletas_service_ng.util.UtilDate;
import com.google.common.io.ByteStreams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AmazonS3ClientServiceImpl implements AmazonS3ClientService {

    private final S3Client s3client;
    //private final RegisterFileUploadFeign registerFileUploadFeign;
    //private final SummaryDocumentsFeign summaryDocumentsFeign;

    private final RegisterFileUploadRestService registerFileUploadRestService;
    private final SummaryDocumentsRestService summaryDocumentsRestService;

    @Value("${apifact.aws.s3.bucket}")
    private String bucketName;

    @Value("${apifact.aws.s3.baseUrl}")
    private String baseUrl;

    @Override
    public RegisterFileUploadDto uploadFileStorage(InputStream inputStream, String nameFile, String folder, CompanyDto companyDto) {
        String periodo = UtilDate.dateNowToString("MMyyyy");

        // Bucket fijo (sin '/')
        String bucket = this.bucketName;

        // key (ruta dentro del bucket)
        String fileNameKey = String.format("archivos/%s/%s/%s/%s-%s",
                companyDto.getRuc(),
                folder,
                periodo,
                UUID.randomUUID(),
                nameFile);
        try {
            StopWatch watch = new StopWatch();
            watch.start();

            byte[] contentBytes = IOUtils.toByteArray(inputStream);

            byte[] md5Bytes = DigestUtils.md5(contentBytes);
            String md5Base64 = Base64.encodeBase64String(md5Bytes);

            Map<String, String> metadata = new HashMap<>();
            metadata.put("original-filename", nameFile);
            metadata.put("uploaded-by", companyDto.getRuc());

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(fileNameKey)
                    .contentMD5(md5Base64)
                    .metadata(metadata)
                    .build();

            s3client.putObject(putObjectRequest, RequestBody.fromBytes(contentBytes));

            RegisterFileUploadDto resp = registerFileUploadRestService.saveRegisterFileUpload(RegisterFileUploadDto.builder()
                    .estado("A")
                    .bucket(bucket)
                    .nombreGenerado(fileNameKey)
                    .nombreOriginal(nameFile)
                    .codCompany(companyDto.getId())
                    .fechaUpload(new Timestamp(System.currentTimeMillis()))
                    .build());

            watch.stop();
            LogHelper.infoLog(LogMessages.currentMethod(),
                    "El archivo se ha subido exitosamente, nombre=" + nameFile + ", tiempo=" + watch.getTime() + "ms");

            return resp;

        } catch (Exception ex) {
            LogHelper.errorLog(LogMessages.currentMethod(),
                    "Error [" + ex.getMessage() + "] occurred while uploading [" + nameFile + "] ", ex);
            throw new ServiceException("Ocurrió un error al subir el archivo: " + ex.getMessage());
        }

    }

    @Override
    public ByteArrayResource downloadFileInvoice(Long id, String uuid, TipoArchivoEnum tipoArchivoEnum) throws IOException {
        String tipo = tipoArchivoEnum.name();
        RegisterFileUploadDto registerFileUploadDto = null;
        if(tipoArchivoEnum.equals(TipoArchivoEnum.CDR)) {
            Long idDccumentSummary = summaryDocumentsRestService.getIdDocumentSummaryByIdPaymentVoucher(id);
            System.out.println("idDccumentSummary " + idDccumentSummary);
            registerFileUploadDto = registerFileUploadRestService.getDataForCdr(idDccumentSummary, uuid, tipo);
        } else {
            registerFileUploadDto = registerFileUploadRestService.getDataForXml(id, uuid, tipo);
        }
        ByteArrayInputStream is = downloadFileStorage(registerFileUploadDto);
        byte[] targetArray = ByteStreams.toByteArray(is);

        ByteArrayResource resource = new ByteArrayResource(targetArray);
        return resource;
    }

    @Override
    public ByteArrayInputStream downloadFileStorage(RegisterFileUploadDto fileStorage) {
        System.out.println("FILESTORAGE: "+fileStorage);
        String bucket, name;
        if (fileStorage == null ) {
            return new ByteArrayInputStream(new byte[0]);
        }
        if (fileStorage.getIsOld() == null || !fileStorage.getIsOld()) {
            bucket = fileStorage.getBucket();
            name = fileStorage.getNombreGenerado();
        } else {
            bucket = String.format("%s/archivos_old/%s", this.bucketName, fileStorage.getRucCompany());
            name = String.format("%s.%s", fileStorage.getUuid(), fileStorage.getExtension());
        }
        System.out.println("bucket: "+bucket);
        System.out.println("name: "+name);
        ByteArrayInputStream ba = null;
        try{
            ba = new ByteArrayInputStream(getFile(bucket, name).toByteArray());
        } catch (Exception e){
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
        return ba;
    }

    public ByteArrayOutputStream getFile(String bucketName, String keyName) {
        try {
            StopWatch watch = new StopWatch();
            watch.start();

            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(keyName)
                    .build();

            ResponseInputStream<GetObjectResponse> s3Object = s3client.getObject(getObjectRequest);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int len;
            try (InputStream is = s3Object) {
                while ((len = is.read(buffer)) != -1) {
                    baos.write(buffer, 0, len);
                }
            }

            watch.stop();
            log.info("Archivo descargado desde S3 correctamente. bucket={}, key={}, tiempo={}ms",
                    bucketName, keyName, watch.getTime());

            return baos;

        } catch (Exception ex) {
            log.info("NO SE ENCONTRO ARCHIVO EN S3, BUCKET: {} NAME: {}", bucketName, keyName);
            log.error("Exception SERVICE : {}", ex.getMessage());
            throw new ServiceException("El servicio de storage está fuera de servicio, comuníquese con el administrador.", ex);
        }
    }

}
