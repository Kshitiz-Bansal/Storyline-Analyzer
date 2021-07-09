
f1 = open('my_output.txt', 'r')
f2 = open('correct_output.txt', 'r')
fw = open('eureka.txt', 'w')

c1 = f1.readlines()
c2 = f2.readlines()

count = 0
for i in range(len(c2)):
	if(c1[i] == c2[i]):
		fw.write("ok\n")
	else:
		count+=1
		fw.write(c1[i].rstrip() + " " + c2[i]);

print(count)
fw.close()
