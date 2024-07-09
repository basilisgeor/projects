import requests
import base64

client_id = '672c5d6749a54c0eae43e7ccf8cac222'
client_secret = 'a98f6fd483a243a682072d242a3ab392'
credentials = f"{client_id}:{client_secret}"
encoded_credentials = base64.b64encode(credentials.encode('utf-8')).decode('utf-8')

headers = {
    'Authorization': f'Basic {encoded_credentials}',
    'Content-Type': 'application/x-www-form-urlencoded'
}

data = {
    'grant_type': 'client_credentials'
}

response = requests.post('https://accounts.spotify.com/api/token', headers=headers, data=data)

if response.status_code == 200:
    access_token = response.json()['access_token']
    print(f"Access token: {access_token}")
else:
    print("Error obtaining access token:", response.status_code, response.text)
