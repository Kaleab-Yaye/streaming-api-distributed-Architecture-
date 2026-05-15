import boto3
from dataclasses import dataclass
import os


@dataclass 
class S3ClinetHandler:
    s3 = boto3.client(
        's3',
        # all this shit also need to be enviroment variable
    endpoint_url=os.getenv("S3_URI"),  # Your MinIO VM
    aws_access_key_id=os.getenv("S3_ACCESS_KEY"),
    aws_secret_access_key=os.getenv("S3_SECRET_ACCESS_KEY"),
    region_name= 'us-east-1',

    )

    def download_object(self, bucket:str, key:str, save_to_location:str)->bool:
        try:
            self.s3.download_file(bucket, key, save_to_location)
        except  Exception as ex:
            print(f"there was an error with downlaoding a file with the bucket:{bucket} and key:{key}. was trying to save it to the location {save_to_location} Exception thrown was: {ex}")
            return False
        return True
    def upload_object(self, bucket:str, key:str, upload_from_location:str)->bool:
        try:
            self.s3.upload_file(upload_from_location, bucket, key)
        except Exception as ex:
              print(f"there was an error with downlaoding a file with the bucket:{bucket} and key:{key}. was trying to upload from the location {upload_from_location} Exception thrown was: {ex}")
              return False
        return True
    
    def delete_object(self, bucket:str, key:str)->bool:
        try:
            self.s3.delete_object (
                Bucket = bucket, Key = key
            )
        except Exception as ex:
            print (f"there was an error with delteing an object with the bucket:{bucket} and key:{key}. the exception encounterd is {ex} ")
            return False
        return True
    
