# SuperClass Demo

## What you need to run it

### First check out this repo.

### ngrok Domain

Start an ngrok instance: 
 
    ngrok http 4567

Make note of the ngrok Url - you will pass that in as
 
    -DDomain={yourunique}.ngrok.io

### An account Sid and authToken
Capture your account sid and auth token from the console.

Pass them in as 

     -DAccountSid={your-account-sid}
     -DAuthToken={your-auth-token}
     
### Specify Phonenumbers to use (Optional) for outbound calls
You can pass in two phone numbers, the from number is one of the phone numbers you own in your account.
And the to number is the number receiving the call, typically your demo phone.

     -DfromNumber=+1XXXXXXXXXX
     -DtoNumber=+1YYYYYYYYY

### Configure a phone number for inbound calls
Go to the Phone number configuration in the Console and put in the url 
http://{your domain}.ngrok.io/conference as the voice URL.

### Putting it all together - build and launch a service

Go to your source code repository and enter this command:
    
    mvn clean install exec:java  -DAuthToken="xxxxxx" -DAccountSid="ACxxxx" -DDomain="{your ngrok domain}.ngrok.io" -DfromNumber={Your Twilio number} -DtoNumber=+15555555555