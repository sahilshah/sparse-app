import os, errno, shutil
def clearws():
	print('test')
	folder = 'data/ws/'
	for f in os.listdir(folder):
		fname = os.path.join(folder, f)
		try:
			os.remove(fname)
			print('deleteing ' + fname)
		except OSError as e: 
			if e.errno != errno.ENOENT:
				raise

clearws()