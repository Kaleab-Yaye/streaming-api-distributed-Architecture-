import requests 

class BaseURL:
    get_new_job = "http://api:8080/api/hooks/encode/fetch/new"
    issue = "http://api:8080/api/hooks/encode/issue"
    done = "http://api:8080/api/hooks/encode/done"

class NewJob:
    def __init__(self, vid_id:str , vid_location:str, status_code:int ):
        self.vid_id = vid_id
        self.vid_location = vid_location
        self.status_code = status_code

class IssueReport:
    def __init__(self, vidId:str, notVid:bool, brokenVid:bool, issueNotSpecified:bool, fileDeleted:bool):
        self.vidId = vidId
        self.notVid = notVid
        self.brokenVid = brokenVid
        self.issueNotSpecified = issueNotSpecified
        self.fileDeleted = fileDeleted

class JobDone:
    def __init__(self, vidId:str, rawDeleted:bool, finalLocation:str, length:str):
        self.vidId = vidId
        self.finalLocation = finalLocation
        self.rawDeleted = rawDeleted
        self.length = length

         

def get_new_job () -> NewJob:
     get_new_work = requests.get(BaseURL.get_new_job)
     if get_new_work.status_code == 204:
         return  NewJob(None, None, 204 )
     elif get_new_work.status_code == 200:
         get_new_work_json =  get_new_work.json()
         return NewJob(get_new_work_json["vidId"], get_new_work_json["vidLocation"], 200)
     

def report_issue (issueReport: IssueReport)->int:
    EncodeFailedRequest = {'vidId': issueReport.vidId, 'notVid':issueReport.notVid, 'brokenVid': issueReport.brokenVid, 'issueNotSpecified': issueReport.issueNotSpecified,'fileDeleted':issueReport.fileDeleted}
    
    response = requests.post( BaseURL.issue, json=EncodeFailedRequest )
    return response.status_code

def successJob(jobDone: JobDone)->int:
    EncodeDoneRequest = {'vidId':jobDone.vidId, 'rawDeleted':jobDone.rawDeleted, 'finalLocation':jobDone.finalLocation, 'length':jobDone.length }
    print("::::: the loaded id is ", jobDone.vidId)
    response = requests.post(BaseURL.done, json=EncodeDoneRequest )
    return response.status_code

what ist eh reaosn that