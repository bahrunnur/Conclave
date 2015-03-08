import ast
import sys
import argparse
import urllib2

import requests
import oauth2 as oauth
from BeautifulSoup import BeautifulSoup

SMS_OATUH_KEY = "hardhack_0006"
SMS_OAUTH_SECRET = "AZ0H8"

class ConclaveAction(argparse.Action):

    def send_sms(self):
        consumer = oauth.Consumer(key=SMS_OATUH_KEY, secret=SMS_OAUTH_SECRET)

        payload = {"sendSMS":{"pinRequestID":"1","pinDestAddress":"62813205997444","pinMessageBody":"tes send sms","pinShortCode":"9147"}}
        r = requests.post("http://sandbox.appprime.net/TemanDev/rest/sendSMS/", data=payload)

    def listing(self):
        # data = {}
   	    response = urllib2.urlopen('http://192.168.43.177')
    	html = response.read()
    	soup = BeautifulSoup(html)
    	# tuple = [(str(element['name']), str(element['value'])) for element in soup.findAll('input')
	    datas = [{'name': element['name'], 'value': element['value']} for element in soup.findAll('input')]
    	# [data.update({k:v}) for k,v in tuple]
    	# print tuple
    	print(datas)

    def signaling(self, namespace):
        node = namespace.node
        node_id = namespace.node_id
        power = namespace.power

        # do request to slave node

        send_sms(node, node_id, power)

    def __call__(self, parser, namespace, values, option_string=None):
        if option_string == "ls":
            listing()
        else:
            setattr(namespace, self.dest, values)
            signaling(namespace)


def main():
	parser = argparse.ArgumentParser(prog='conclave.py')
	parser.add_argument('--node', dest='node', action=ConclaveAction)
	parser.add_argument('--id', dest='node_id', action=ConclaveAction)
    parser.add_argument('--power', dest="power", action=ConclaveAction)


if __name__ == "__main__":
    main()
