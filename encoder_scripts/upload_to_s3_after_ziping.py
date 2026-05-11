from s3_handler import *
import zipfile
import os

s3_clinet = S3ClinetHandler()

def zip_hls(source_path:str, destination:str)->bool:
    with zipfile.ZipFile(destination, w , compression=zipfile.ZIP_STORED) as the_zip:
        try:
            for root, dirs, files in os.walk(source_path):
                for file_name in files:
                    absolute_path =  os.path.join(root, file_name)
                    path_inside_zip_file = os.path.realpath(absolute_path, source_path)
                    the_zip.write(absolute_path, path_inside_zip_file)
        except Exception as ex:
            print(f"ziping the file in the location {source_path} was not possible becouse of the exception  {ex}")
            return False
    return True


def upload_to_s3(zip_to_be_uploaded:str, key: str, bucket:str)->bool:
    try:
        s3_clinet.upload_object(bucket, key, zip_to_be_uploaded)
    except Exception as ex:
        print(f"couldt upload the zip file with the name {key} the exception was {ex}")
        return False
    return True

def zip_and_upload(source_path:str, zip_destinatinon:str, key:str , bucket:str):
    zip_result = zip_hls(source_path, zip_destinatinon )
    if(not zip_result):
        return [False, False]
    upload_to_s3_result = upload_to_s3 (zip_destinatinon, key, bucket)
    if(not upload_to_s3_result):
        return [True, False]
    return [True, True]


    