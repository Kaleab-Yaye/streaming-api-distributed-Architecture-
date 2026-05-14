import zipfile

def unzip_hls(zip_path: str, destination: str) -> bool:
    with zipfile.ZipFile(zip_path, 'r') as the_zip:
        try:
            the_zip.extractall(destination)
        except Exception as ex:
            print(f"unzipping {zip_path} failed: {ex}")
            return False
    return True
