import requests 
import os

class BaseURL:
    # this whole shit needs to be made an Enviroment variable than a static one
    
   base_hook = os.getenv("CENTERAL_CERV")
   get_new_job = base_hook + ":8080/api/hooks/encode/fetch/new"
   issue = base_hook + ":8080/api/hooks/encode/issue"
   done = base_hook + ":8080/api/hooks/encode/done"

    

class NewJob:
    def __init__(self, vid_id:str , vid_location:str, status_code:int ):
        self.vid_id = vid_id
        self.vid_location = vid_location
        self.status_code = status_code

class IssueReport:
    def __init__(self, vidId:str, notVid:bool, brokenVid:bool, issueNotSpecified:bool, fileDeleted:bool, objectDeltedFromS3:bool):
        self.vidId = vidId
        self.notVid = notVid
        self.brokenVid = brokenVid
        self.issueNotSpecified = issueNotSpecified
        self.fileDeleted = fileDeleted
        self.objectDeltedFromS3 = objectDeltedFromS3

class JobDone:
    def __init__(self, vidId:str, rawDeleted:bool, finalLocation:str, length:str, objectDeltedFromS3:bool, s3ObjectZipped:bool, s3ObjectUploaded:bool):
        self.vidId = vidId
        self.finalLocation = finalLocation
        self.rawDeleted = rawDeleted
        self.length = length
        self.objectDeltedFromS3 = objectDeltedFromS3
        self.s3ObjectUploaded = s3ObjectUploaded
        self.s3ObjectZipped = s3ObjectZipped

 
 
 
 
header = {
         'Mech-Number':os.getenv("MECH_NUMBER")
     }  
  

def get_new_job () -> NewJob:
    
     get_new_work = requests.get(BaseURL.get_new_job, headers=header)
     if get_new_work.status_code == 204:
         return  NewJob(None, None, 204 )
     elif get_new_work.status_code == 200:
         get_new_work_json =  get_new_work.json()
         return NewJob(get_new_work_json["vidId"], get_new_work_json["vidLocation"], 200)
     

def report_issue (issueReport: IssueReport)->int:
    EncodeFailedRequest = {'vidId': issueReport.vidId, 'notVid':issueReport.notVid, 'brokenVid': issueReport.brokenVid, 'issueNotSpecified': issueReport.issueNotSpecified,'fileDeleted':issueReport.fileDeleted , 'fileDeletedFromS3':issueReport.objectDeltedFromS3}
    
    response = requests.post( BaseURL.issue, json=EncodeFailedRequest, headers=header )
    return response.status_code

def successJob(jobDone: JobDone)->int:
    EncodeDoneRequest = {'vidId':jobDone.vidId, 'rawDeleted':jobDone.rawDeleted, 'finalLocation':jobDone.finalLocation, 'length':jobDone.length, 's3ObjectDeleted':jobDone.objectDeltedFromS3,'s3ObjectZipped':jobDone.s3ObjectZipped, 's3ObjectUploaded':jobDone.s3ObjectZipped }
    print("::::: the loaded id is ", jobDone.vidId)
    response = requests.post(BaseURL.done, json=EncodeDoneRequest, headers= header )
    return response.status_code

