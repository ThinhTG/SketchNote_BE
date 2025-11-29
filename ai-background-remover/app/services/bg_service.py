from rembg import remove
# from rembg import remove, new_session

# my_session = new_session("u2netp")

async def remove_background_service(file):
    input_bytes = await file.read()
    output_bytes = remove(input_bytes)
    return output_bytes
