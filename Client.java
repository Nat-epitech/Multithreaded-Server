import java.io.*;
import java.net.*;
import java.net.Socket;
import java.util.Scanner;
import java.util.UUID;



class ClientM {  
    private final static int portNum = 2020;
    FunctionPointer[] functionPointersArray = new FunctionPointer[6];
    Scanner scan = new Scanner(System.in);
    String registration;
    DataInputStream in;
    ObjectOutputStream  oos;
    Socket socket;
    int request;
    

    private interface FunctionPointer {
        Car methodSignature();
      }

    ClientM(){
        UUID randomUUID = UUID.randomUUID();
        registration = randomUUID.toString().replaceAll("-", "");
        registration = registration.substring(0, registration.length() - 24);
    }
    
    public Car add_car() {
        System.out.print("\nAdd car:\nMake: ");
        String make = scan.next();
        System.out.print("Price: ");
        int price = scan.nextInt();
        System.out.print("Mileage: ");
        int mileage = scan.nextInt();
        return (new Car(registration, make, price, mileage, 0));
    }

    public Car sell_car() {
        System.out.print("\nSell car:\nRegistation: ");
        String car_regi = scan.next();
        System.out.print("Make: ");
        String make = scan.next();
        return (new Car(car_regi, make, 0, 0, 1));
    }
    
    public Car request_all_make() {
        System.out.print("\nSearch all make:\nMake: ");
        String make = scan.next();
        return (new Car(registration, make, 0, 0, 2));
    }
    
    public Car request_all_car() {
        return (new Car(3));
    }

    public Car request_total_price() {
        return (new Car(4));
    }

    public Car exit_client() {
        return (new Car(5));
    }


    public void complete_request(int request){
        Car car = functionPointersArray[request].methodSignature();
        
        try {
            oos.writeObject(car);
            if (request != 5)
                System.out.println(in.readUTF());
        }catch(IOException e){e.printStackTrace();}
    }

    public void run() {
        functionPointersArray[0] = this::add_car;
        functionPointersArray[1] = this::sell_car;
        functionPointersArray[2] = this::request_all_make;
        functionPointersArray[3] = this::request_all_car;
        functionPointersArray[4] = this::request_total_price;
        functionPointersArray[5] = this::exit_client;
        try {
            socket = new Socket(InetAddress.getLocalHost(),portNum);
            in = new DataInputStream(socket.getInputStream());
            oos = new ObjectOutputStream(socket.getOutputStream());
            while (true){
                System.out.print("[0] Add a car\n[1] Sell a car\n[2] Request all car of given make\n[3] Request all car\n[4] Request total value of all sales\n[5] Exit\n\nChoice:");
                request = scan.nextInt();
                complete_request(request);
                if (request == 5)
                    break;
            }
            scan.close();
            oos.close();
        } catch (IOException e) {System.out.println(e);}
    }
}

public class Client {
    public static void main(String[] args){
        ClientM client = new ClientM();
        client.run();
    }
}