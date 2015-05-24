import subprocess
import json
from flask import Flask,jsonify,request, Response
import os, errno, shutil
app = Flask(__name__)

def clearws():
	folder = 'data/ws/images/'
	for f in os.listdir(folder):
		fname = os.path.join(folder, f)
		try:
			os.remove(fname)
		except OSError as e: 
			print("Could not delete")
			if e.errno != errno.ENOENT:
				raise

test_dir = 'tesla'

@app.route("/test")
def test():
	# certain api called with all images as data
	# save images in data/new_data
	subprocess.call(['bin/run_all','data/' + test_dir])
	with open('data/' + test_dir + '/reconstruction.json', 'r') as content_file:
		content = content_file.read()
	data = json.loads(content)
	return jsonify(results=data)
	# on the app side find a way to parse json and display as 3d points

@app.route("/test2",methods=['POST'])
def test2():
	# subprocess.call(['bin/run_all','data/' + test_dir])
	with open('data/' + test_dir + '/reconstruction.json', 'r') as content_file:
		content = content_file.read()
	data = json.loads(content)
	return jsonify(results=data)


# MAIN CODE STARTS HERE

@app.route('/upload', methods=['GET', 'POST'])
def upload_file():
	if request.method == 'POST':
		clearws()
		print('Deleted files in WS..')
		i = 0
		while(True):
			try:
				f = request.files['img'+str(i)]
				f.save('data/ws/images/img' + str(i) + '.jpg')
				i += 1
			except KeyError:
				break
		subprocess.call(['bin/run_all','data/ws'])
		with open('data/ws/reconstruction.json', 'r') as content_file:
			content = content_file.read()
		resp = Response(content, status=200, mimetype='application/json')
		return resp

if __name__ == "__main__":
	app.run(host='0.0.0.0')