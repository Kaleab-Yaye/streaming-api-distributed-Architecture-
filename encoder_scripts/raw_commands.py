import shlex;


def get_command (command_num: int, comb_input_path: str, dest_path: str) -> list[str]:
    probe_check = f'''ffprobe -v error\
          -select_streams v:0 -show_entries\
             stream=nb_read_frames -count_frames\
                  -read_intervals "%+#20"\
                    -of default=nokey=1:noprint_wrappers=1\
                         {comb_input_path}'''
    
    decode_check = f'''ffmpeg -v error -xerror -i {comb_input_path} -f null -'''
    find_length = f'''ffprobe -v error -show_entries format=duration -of default=noprint_wrappers=1:nokey=1 {comb_input_path} '''
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
  -hls_segment_filename {dest_path}"/stream_%v/segment_%03d.ts" \
    "{dest_path}/stream_%v/playlist.m3u8"'''


    match command_num:
        case 1:
            return shlex.split(probe_check)
        case 2:
            return shlex.split(decode_check)
        case 3:
            return shlex.split(encode)
        case 4:
            return shlex.split(find_length)
        