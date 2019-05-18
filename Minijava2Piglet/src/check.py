import os

items = ["BinaryTree", "BubbleSort", "Factorial", "LinearSearch", "LinkedList", "MoreThan4", "QuickSort", "TreeVisitor"]

def check(item):
        print("-----------------TEST " + item + "--------------------")
        os.system("wget http://compilers.cs.ucla.edu/cs132/project/pg/" + item + ".pg -q")
        os.system("wget http://compilers.cs.ucla.edu/cs132/project/mj/" + item + ".java -q")
        os.system("java Main " + item + ".java > temp.pg")

        os.system("java -jar pgi.jar < temp.pg > res0")
        os.system("java -jar pgi.jar < " + item + ".pg > res1")
        os.system("diff res0 res1")
        
        os.system("rm " + "res0")
        os.system("rm " + "res1")
        os.system("rm " + item + ".java")
        os.system("rm " + item + ".pg")
        os.system("rm " + "temp.pg")

for i in items:
        check(i)       