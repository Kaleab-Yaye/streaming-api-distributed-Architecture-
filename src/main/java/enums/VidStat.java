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
  UPLOADED_NOT_DELETED, // when video has completed upload and is stored local on the API server but
  // is not DELETED YET
  UPLOADED_DELETION_FAILED // when spring is not able to delete the uploaded vid
}
