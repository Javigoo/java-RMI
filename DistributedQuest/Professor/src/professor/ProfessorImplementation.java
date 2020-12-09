package professor;

import common.ProfessorInterface;
import common.StudentInterface;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

/**
 *
 */
class Exam {
    List<Question> questions;
    Integer questionIterator;
    Boolean examStarted;

    public Exam(){
        this.questions = new ArrayList<>();
        this.questionIterator = 0;
        this.examStarted = false;
    }

    public static class Question {
        List<String> choices;
        Integer correctChoice;

        public Question(List<String> choices, Integer correctChoice) {
            this.choices = choices;
            this.correctChoice = correctChoice;
        }

        public Boolean isTheCorrectResponse(Integer response){
            if (response==this.correctChoice){
                return true;
            }
            return false;
        }

        public List<String> getChoices(){
            return this.choices;
        }

        @Override
        public String toString() {
            return "Question{" +
                    "choices=" + choices +
                    ", correctChoice=" + correctChoice +
                    '}';
        }
    }

    public Boolean isCorrectAnswer(Integer question, Integer response){
        return this.questions.get(question).isTheCorrectResponse(response);
    }


    public Question getNextQuestion(){
        if (this.questionIterator <= this.questions.size()){
            Question nextQuestion = this.questions.get(this.questionIterator);
            this.questionIterator += 1;
            return nextQuestion;
        }
        return null; // Finalizar examen
    }

    public void startExam(){
        this.examStarted = true;
    }

    public Boolean isStarted(){
        return this.examStarted;
    }

}

public class ProfessorImplementation extends UnicastRemoteObject implements ProfessorInterface {
    HashMap<String, StudentInterface> students = new HashMap<>();
    Exam exam = new Exam();

    public ProfessorImplementation() throws RemoteException{
        super();
    }

    public void uploadCSV(File csv){
        try (BufferedReader br = new BufferedReader(new FileReader(csv))) {
            String line;
            while ((line = br.readLine()) != null) {
                List<String> choices = new ArrayList<>();
                Integer correctChoice;

                List<String> values = Arrays.asList(line.split(";"));

                for (int i = 1; i<values.size()-1;i++) {
                    choices.add(values.get(i));
                }

                correctChoice = Integer.parseInt(values.get(values.size() - 1));

                this.exam.questions.add(new Exam.Question(choices, correctChoice));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void waitStudents(Integer studentsNumber) throws InterruptedException {
        while (this.getNumStudents() < studentsNumber) {
            this.wait();
            System.out.println("Number of students joined: [" + this.getNumStudents() + "/" + studentsNumber +"]");
        }
        System.out.println("Number of students joined: [" + this.getNumStudents() + "/" + studentsNumber +"]");

    }

    public int getNumStudents(){
        return students.size();
    }

    public void joinExam(String id, StudentInterface student) throws RemoteException {
        if (exam.isStarted()){
            student.notifyAlreadyStarted();
        }
        synchronized (this) {
            students.put(id, student);
            this.notify();
        }
        System.out.println("Student: " + id + " joined, waiting for notification");
    }

    public void startExam(){
        System.out.println("The professor has started the exam");
        this.exam.startExam();

        List<StudentInterface> error_students = new ArrayList<StudentInterface>();
        for (StudentInterface student : students.values()) {
            try{
                student.notifyStart();
            }catch(RemoteException e){
                System.out.println("Student is not reachable");
                error_students.add(student);
            }
        }
        for(StudentInterface c: error_students){
            this.students.remove(c);
        }
    }


    /**
     *  ####################################################################################
     */

    HashMap<StudentInterface,Integer> clientNumber = new HashMap<>();
    int answers = 0;

    public void sendAnswerNumber(StudentInterface client, int number) throws RemoteException{
        synchronized (this) {
            //System.out.println(number);
            clientNumber.put(client, number);
            answers ++;
            this.notify();
        }
    }

    public List<StudentInterface> getWinners(int number){
        List<StudentInterface> returns = new ArrayList<>();
        for (StudentInterface client: students.values()){
            //System.out.println(clientNumber.get(client));
            //System.out.println(number);

            if (clientNumber.get(client) == number){
                returns.add(client);
            }
        }
        return returns;
    }

    public List<StudentInterface> getLoosers(int number){
        List<StudentInterface> returns = new ArrayList<>();
        for (StudentInterface client: students.values()){
            if (clientNumber.get(client) != number){
                returns.add(client);
            }
        }
        return returns;
    }

    public void restart(){
        this.students.clear();
        this.answers = 0;
        this.clientNumber.clear();
    }

}
