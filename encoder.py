from pathlib import Path;
import os;
import requests;
import time;
import shlex;
import subprocess;

time.sleep(15)
while True:
    break
    fet_new_req = requests.get("http://api:8080/api/hooks/encode/fetch/new")
    if not fet_new_req.ok:
        print("we got {}", fet_new_req.status_code)
        time.sleep(30)
        continue

    payload = fet_new_req.json()

    source_path = payload["source"]
    dest_path = Path(payload["destination"])
    env_input_path = os.getenv("RAW_VID")
    env_outPut_path = os.getenv("PROC_ED")

    comb_input_path = (env_input_path+"/"+source_path)

    dest_path.mkdir(exist_ok=True)
    print("we recived ", source_path, "and" , dest_path)
    encode = f''' ffmpeg -i {comb_input_path} \
  -filter_complex \
  "[0:v]split=3[v1][v2][v3]; \
   [v1]scale=w=-2:h=720[v720]; \
   [v2]scale=w=-2:h=480[v480]; \
   [v3]scale=w=-2:h=360[v360]" \
  -map "[v720]" -map "[v480]" -map "[v360]" -map 0:a -map 0:a -map 0:a \
  -c:v libx264 -c:a aac \
  -b:v:0 2500k -b:v:1 1000k -b:v:2 600k -b:a:0 128k -b:a:1 128k -b:a:2 128k \
  -f hls \
  -hls_time 10 \
  -hls_playlist_type vod \
  -hls_flags independent_segments \
  -var_stream_map "v:0,a:0,name:720 v:1,a:1,name:480 v:2,a:2,name:360" \
  -master_pl_name  "master.m3u8"
  -hls_segment_filename {env_outPut_path}{payload['destination']}"/stream_%v/segment_%03d.ts" \
    "{env_outPut_path}{payload['destination']}/stream_%v/playlist.m3u8"'''

    encod_command = shlex.split(encode)
    print(encod_command)

    runer = subprocess.run(
        encod_command,
        cwd = dest_path,
        capture_output= True,
        text= True,
    )

    print(" ###### we got a return ####  " + str(runer.returncode) + " " + runer.stderr)

    break






