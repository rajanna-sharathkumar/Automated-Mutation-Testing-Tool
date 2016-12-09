import os
import shutil
import subprocess
import time

def runcmd(cmd):
    proc = subprocess.Popen(cmd,stdout=subprocess.PIPE, stderr=subprocess.PIPE, shell=True )
    outs = "timeout"
    try:
        outs, errs = proc.communicate(timeout=120)
    except TimeoutError:
        proc.kill()
        outs, errs = proc.communicate()
    finally:
        # print("yo!")
        proc.kill()
        return outs


def readReport():
    file = open('MutationReport.txt', 'r')
    lines = file.readlines()[0:5]

    # print(lines)
    projectName = lines[0].split(";")[1].strip(" \n\t\r")
    projectLoc = lines[1].split(";")[1].strip(" \n\t\r")
    mutatedProjLoc = lines[4].split(";")[1].strip(" \n\t\r")

    # print mutatedProjLoc
    return projectName, projectLoc, mutatedProjLoc


def findMutationScore():
    total_mutants = 0
    killed_mutants = 0
    timedout = 0
    print("STAGE-2: Executing Test cases")
    dirs = os.listdir(mutatedProjLoc)
    if len(dirs) > 0 :
        for file in dirs:
            total_mutants += 1
            loc = mutatedProjLoc + "\\" + file
            print("running: "+loc)
            os.chdir(loc)
            # print loc
            mvn_test_result = str(runcmd(["mvn","test"]))

            if "timeout" in mvn_test_result:
                timedout +=1
                killed_mutants += 1

            if "BUILD FAILURE" in mvn_test_result:
                killed_mutants += 1
                # print "BUILD SUCCESS"

        live_mutants = total_mutants - killed_mutants
        mutation_score = killed_mutants / total_mutants
        print("Done running Test cases")
        print("*************************************************************\n")
        print("Timed Out: ",timedout)
        print("Total Mutants: ", total_mutants)
        print("Killed Mutants: ", killed_mutants)
        print("Live Mutants: ", live_mutants)
        print("Mutation Score: ", mutation_score)
        return mutation_score
    return -1

def generate_mutants():

    print("STAGE-1: Select project for Mutation Testing")
    some_command = "java -jar  ..\\target\\com.mutation.testing-0.0.1-SNAPSHOT-jar-with-dependencies.jar"
    p = subprocess.Popen(some_command, stdout=subprocess.PIPE, shell=True)

    (output, err) = p.communicate()

    # This makes the wait possible
    p_status = p.wait()

    # This will give you the output of the command being executed
    # print output
    print("Done Generating Mutants")
    return err

start = time.time()
err = generate_mutants()

print ("Error: ",err)

projectName, projectLoc, mutatedProjLoc = readReport()

print(mutatedProjLoc)
mutation_score = findMutationScore()

# print(mutatedProjLoc)
shutil.rmtree(mutatedProjLoc, ignore_errors=True)
#
if mutation_score == -1:
    print("*************************************************************\n")
    print("No project found in "+mutatedProjLoc)

print("Project Location: "+projectLoc)
print("Mutation Testing Report Location: "+projectLoc+"\\report")
print("Generated Mutants Location: "+projectLoc+"\\mutants")


print("Time Elapsed: ",time.time()-start)
print("\n************************************************************")
