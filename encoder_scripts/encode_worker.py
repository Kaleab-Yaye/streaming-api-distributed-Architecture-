from pathlib import Path
from raw_commands import get_command
from make_request import *
import os
import requests
import time
import shlex
import subprocess
import shutil

class Const:
 time_to_sleep = 30




def extract_env()->tuple[str, str]:
  
  return os.getenv("RAW_VID"), os.getenv("PROC_ED")


def delete_file(location: str)->int:
 to_be_deleted = Path(location)
 to_be_deleted_info = Path(location + ".info")
 print (f" the lcoaiton to be deleted is {location}")

 try:
  to_be_deleted.unlink(missing_ok = True)
  to_be_deleted_info.unlink(missing_ok= True)

 except Exception as ex:
  print(f" unexpexted error while delting a file ex>>>>>>:: {ex}")
  return 1
 
 print("deleted the file")
 return 0 


def run_command(command_num:int, command: list[str])->tuple[int, str]:
 if(command_num== 1 or command_num == 4):
  runer = subprocess.run(
        command,
        capture_output= True,
        text= True,
    )
  return runer.returncode, runer.stdout.strip()
 
 elif(command_num == 2 or command_num ==3):
  runer = subprocess.run(
        command,
        capture_output= True,
        text= True,
    )
  return runer.returncode, None

if __name__ == "__main__":
 time.sleep(12)
 while True:
  print( " making a reqeust")
  new_work = get_new_job()
  print("::: got a response with {new_work.status_code}")
  if (new_work.status_code == 204):
   time.sleep(Const.time_to_sleep)
   continue
  elif (new_work.status_code != 200):
   print("::::::::::::::::::::::iligal server reponse:::::::::::::::::::::::::::::::")


  
  print(":::::: got vide locaito of::::vid and id ", new_work.vid_location, new_work.vid_id)
  vid_loaction = extract_env()[1]+ "/" + new_work.vid_location 
  vid_id = new_work.vid_id

  vid_store_location = extract_env()[0]
  vid_raw_location = vid_store_location + "/" + new_work.vid_location 

  print(":::::: got vid raw location at ::::::::::::: {vid_raw_location}")

  prob_check_command = get_command(1, vid_raw_location, vid_loaction)
  decode_check_command = get_command(2, vid_raw_location, vid_loaction )
  encode_check_command = get_command(3,vid_raw_location, vid_loaction) 
  get_vid_length_check_command =get_command(4,vid_raw_location, vid_loaction )

  #log
  print("::::::::::::::::::running prob check::::::::::::::::::::::")


  prob_check_result = run_command(1, prob_check_command)

  #log
  print("::::::::::::::::::out of prob check with the result {} and status code {}::::::::::::::::::::::", prob_check_result[1], prob_check_result[0])

  if(prob_check_result[0] != 0 or int( prob_check_result[1])<=1):
   issueReport = "holder"
   delete_result = delete_file(vid_raw_location)
   if(delete_result==1):
    issueReport = IssueReport(vid_id, True, False, False, False)

   else:
    issueReport = IssueReport(vid_id, True, False, False, True)
      #log
   print("::::::::::::reporting issue:::::::::::::::::::::::::::::")
   reportResult = report_issue(issueReport)

   #log
   print("::::::::::::reporting issue:::::::::::::::::::::::::::::")
   print("::::::::::::::server responded with the status code}{}:::::", reportResult)

   if(reportResult != 200):
    print("server handling issue responded with iligal status code")
   time.sleep(Const.time_to_sleep)
   continue


#log
  print(":::: runing decode check:::::::::::::::::::")
  
  

  decode_check_result = run_command (2, decode_check_command)

  #log

  print("::: out of decode check with status code", decode_check_result[0])



  if(decode_check_result[0] != 0):
   issueReport = "holder"
   delete_result = delete_file(vid_raw_location)
   if(delete_result==1):
     
     issueReport = IssueReport(vid_id, False, True, False, False)
  
   else:
     issueReport = IssueReport(vid_id, False, True, False, True)
      #log
   print("::::::::::::reporting issue:::::::::::::::::::::::::::::")
   reportResult = report_issue(issueReport)
   #log
   print("::::::::::::reporting issue:::::::::::::::::::::::::::::")
   print("::::::::::::::server responded with the status code}{}:::::", reportResult)

   if(reportResult != 200):
    print("server handling issue responded with iligal status code")
   time.sleep(Const.time_to_sleep)
   continue
  #log
  print("before going to encode check making vid length identification")
  vid_length_result = run_command(4 , get_vid_length_check_command)
  print(f" the length check is out with the status code of :: {vid_length_result[0]}the length of vid is::: {vid_length_result[1]}")
  print(":::: runing encode check:::::::::::::::::::")
  vid_length = float(vid_length_result[1])
  print(f"the length of the vid is {vid_length}")
  encode_check_result = run_command (3, encode_check_command)

  print("::: out of encode check with status code", encode_check_result[0])


  if (encode_check_result[0]!=0):
   ## will add the delting step here for now we will just say false
   issueReport = "holder"
   delete_result = delete_file(vid_raw_location)
   if(delete_result==1):
     
     issueReport = IssueReport(vid_id, False, False, True, False)

   else:
     issueReport = IssueReport(vid_id, False, False, True, True)

   #log
   print("::::::::::::reporting issue:::::::::::::::::::::::::::::")
   reportResult = report_issue(issueReport)
   #log
   print("::::::::::::::server responded with the status code}{}:::::", reportResult)

   if(reportResult != 200):
    print("server handling issue responded with iligal status code")
   time.sleep(Const.time_to_sleep)
   continue 

  jobDone = "holder"

  info_path = vid_raw_location+".info"
  info_dest_path = extract_env()[1]
  try:
   shutil.copy(info_path, info_dest_path)

  except Exception as ex:
   print(f"error coping file {ex}")


  delete_result = delete_file(vid_raw_location)
  if(delete_result==1):
    jobDone = JobDone(vid_id, False, new_work.vid_location, vid_length)
  else:
    jobDone = JobDone(vid_id, True, new_work.vid_location, vid_length )

  print(f"::::::::::::reporting jobe done with {jobDone.length}:::::::::::::::::::::::::::::")

  jobeDoneResponseResult = successJob(jobDone)
  print(":::::::::::: jobe done report from server stuatus code:::::::::::::::::::::::::::::", jobeDoneResponseResult)

  if(jobeDoneResponseResult != 200):
    print(":::::::::::::::::::::::::::::::::::::server handling job complete reqeust responded with iligal status code::::::::::::::::::::::::::::::;")
    time.sleep(Const.time_to_sleep)
    continue
  time.sleep(Const.time_to_sleep)