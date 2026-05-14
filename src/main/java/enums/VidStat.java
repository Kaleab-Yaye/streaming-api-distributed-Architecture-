package enums;

public enum VidStat {
  UPLOADED, // state of vid where the vid is not encoded yet but only is located in the s3 storage
  // space
  APPROVED,
  ENCODED,
  ENCODERR,
  MODEREGCT,
  UPLOADREQ,
  ENCODING,
  REMOVED,
  NOT_VID,
  BROKEN_VID,
  TUS_UPLOAD_COMPLETE,
  CENTRAL_TO_S3_UPLOAD_FAILED, // spring cant upload the local saved file
  UPLOADED_NOT_DELETED, // when video has completed upload and is stored local on the API server but
  // is not DELETED YET
  UPLOADED_DELETION_FAILED, //// when spring is not able to delete the uploaded vid
  ENCODER_DOWNLOAD_FROM_S3_FAILED,
  ENCODER_UPLOAD_TO_S3_FAILED,
  ENCODER_ZIP_FAILED,
  ENCODER_NOT_VID_DELETE_OBJECT_S3_FAILER_FAILED,
  ENCODER_BROKEN_VID_DELETE_OBJECT_S3_FAILER_FAILED,
  ENCODER_ENCODERR_DELETE_OBJECT_S3_FAILER_FAILED,
  ENCODER_DELETE_OBJECT_S3_JOB_DONE_FAILED,
    //streaming state machines
    STREAMING_NODE_DOWNLOADING_FAILED,
    STREAMING_NODE_ZIPPING_FAILED,
    READY
}
