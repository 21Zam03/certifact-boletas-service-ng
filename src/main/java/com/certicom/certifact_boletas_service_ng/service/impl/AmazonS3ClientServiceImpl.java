package com.certicom.certifact_boletas_service_ng.service.impl;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.certicom.certifact_boletas_service_ng.dto.CompanyDto;
import com.certicom.certifact_boletas_service_ng.dto.RegisterFileUploadDto;
import com.certicom.certifact_boletas_service_ng.exception.ServiceException;
import com.certicom.certifact_boletas_service_ng.feign.RegisterFileUploadFeign;
import com.certicom.certifact_boletas_service_ng.service.AmazonS3ClientService;
import com.certicom.certifact_boletas_service_ng.util.UtilDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.sql.Timestamp;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AmazonS3ClientServiceImpl implements AmazonS3ClientService {

    private final RegisterFileUploadFeign registerFileUploadFeign;
    private final AmazonS3 s3client;

    @Value("${apifact.aws.s3.bucket}")
    private String bucketName;

    @Value("${apifact.aws.s3.baseUrl}")
    private String baseUrl;

    @Override
    public RegisterFileUploadDto uploadFileStorage(InputStream inputStream, String nameFile, String folder, CompanyDto companyDto) {
        System.out.println("NOMBRE DEL ARCHIVO " + nameFile);
        String periodo = UtilDate.dateNowToString("MMyyyy");

        // ✅ Bucket fijo (sin '/')
        String bucket = this.bucketName;

        // ✅ Key (ruta dentro del bucket)
        String fileNameKey = String.format("archivos/%s/%s/%s/%s-%s",
                companyDto.getRuc(),
                folder,
                periodo,
                UUID.randomUUID(),
                nameFile);

        try {
            StopWatch watch = new StopWatch();
            watch.start();

            ObjectMetadata metadata = new ObjectMetadata();
            byte[] resultByte = DigestUtils.md5(inputStream);
            inputStream.reset();
            byte[] contentBytes = IOUtils.toByteArray(inputStream);
            String streamMD5 = new String(Base64.encodeBase64(resultByte));
            Long contentLength = Long.valueOf(contentBytes.length);
            metadata.setContentMD5(streamMD5);
            metadata.setContentLength(contentLength);

            inputStream.reset();

            PutObjectRequest putObjectRequest = new PutObjectRequest(bucket, fileNameKey, inputStream, metadata);
            this.s3client.putObject(putObjectRequest);

            RegisterFileUploadDto resp = registerFileUploadFeign.saveRegisterFileUpload(RegisterFileUploadDto.builder()
                    .estado("A")
                    .bucket(bucket)                // 👈 solo el bucket real
                    .nombreGenerado(fileNameKey)   // 👈 aquí guardas la "ruta" completa
                    .nombreOriginal(nameFile)
                    .codCompany(companyDto.getId())
                    .fechaUpload(new Timestamp(System.currentTimeMillis()))
                    .build());

            watch.stop();
            log.info(String.format("%s %s %s", "Tiempo de Subida de archivo:", nameFile, watch.getTime()));
            return resp;

        } catch (Exception ex) {
            ex.printStackTrace();
            log.error("error [" + ex.getMessage() + "] occurred while uploading [" + nameFile + "] ");
            throw new ServiceException("Ocurrio un error al subir el archivo: " + ex.getMessage());
        }
    }

}
