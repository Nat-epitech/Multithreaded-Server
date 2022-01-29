import java.util.ArrayList;
import java.net.ServerSocket;
import java.io.*;
import java.net.Socket;
import java.util.concurrent.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class Stock {
    ArrayList<Car> carList = new ArrayList<Car>();
    Lock lock = new ReentrantLock();
	Condition port = lock.newCondition();

    Stock(){
        carList.add(new Car("123stock", "ferrari", 130000, 5000, 0));
        carList.add(new Car("123stock", "tesla", 145000, 6700, 0));
        carList.add(new Car("123stock", "bmw", 28000, 400, 0));
        carList.add(new Car("123stock", "toyota", 19000, 240, 0));
        carList.add(new Car("123stock", "honda", 115000, 1900, 0));
        carList.add(new Car("123stock", "ford", 9000, 7030, 0));
        carList.add(new Car("123stock", "audi", 35000, 2160, 0));
        carList.add(new Car("123stock", "nissan", 7500, 9480, 0));
        carList.add(new Car("123stock", "porsche", 30000, 4900, 0));
        carList.add(new Car("123stock", "land rover", 10500, 4890, 0));
    }

    public synchronized String add_car(Car car){
        String s = "\nAdd a Car:\n";
        lock.lock();
        try{
            carList.add(car);
            s += car.make + " added successfully\n";
            return (s);
        }
        finally{lock.unlock();}
    }

    public synchronized String sell_car(Car car){
        String s = "\nSell a Car:\n";

        lock.lock();
        try{
            if (carList.isEmpty())
                return (s + "No cars in stock\n");
            for (int i = 0; i < carList.size(); i++){
                if (carList.get(i).make.equals(car.make) && carList.get(i).registration.equals(car.registration)){
                    carList.remove(i);
                    s += car.registration + " sold successfully\n";
                    return (s);
                }
            }
            return (s + "No cars founded\n");
        }
        finally{lock.unlock();}
    }

    public synchronized String get_all_cars(){
        String s = "\nCars for Sale:\n";

        lock.lock();
        try{
            if (carList.isEmpty()){
                s += "No cars in stock\n";
                return (s);
            }
            for (int i = 0; i < carList.size(); i++)
                s += "[registration = " + carList.get(i).registration + ", make = " + carList.get(i).make + ", price = " + carList.get(i).price + ", mileage = " + carList.get(i).mileage + ", forSale = true]\n";
            return (s);
        }
        finally{lock.unlock();}
    }

    public synchronized String get_make(Car car){
        String s = "\nSearch By Make:\n";
        boolean found = false;

        lock.lock();
        try{
            if (carList.isEmpty()){
                s += "No cars in stock\n";
                return (s);
            }
            for (int i = 0; i < carList.size(); i++){
                if (carList.get(i).make.equals(car.make)){
                    s += "[registration = " + carList.get(i).registration + ", make = " + carList.get(i).make + ", price = " + carList.get(i).price + ", mileage = " + carList.get(i).mileage + ", forSale = true]\n";
                    found = true;
                }
            }
            if (found == false)
                s += "No cars founded with |" + car.make + "| as make\n";
            return (s);
        }
        finally{lock.unlock();}
    }

    public synchronized String get_total(){
        String s = "\nSearch Total Value:\n";
        double total = 0;

        lock.lock();
        try{
            if (carList.isEmpty()){
                s += "No cars in stock\n";
                return (s);
            }
            for (int i = 0; i < carList.size(); i++)
                total += carList.get(i).price;
            s += "Total = " + total + "\n";
            return (s);
        }
        finally{lock.unlock();}
    }
}

class clientManager extends Thread {
    Socket socket;
    Stock stock;
    Semaphore semaphore;
    DataOutputStream out;
    FunctionPointer[] functionPointers = new FunctionPointer[2];
    FunctionPointerCar[] functionPointersCar = new FunctionPointerCar[3];
    ObjectInputStream ois;

    private interface FunctionPointer {
        String method();
    }

    private interface FunctionPointerCar {
        String method(Car car);
      }

    clientManager(Socket socket, Semaphore semaphore, Stock stock){
        this.socket = socket;
        this.semaphore = semaphore;
        this.stock = stock;
    }

    public void complete_request(Car car){
        String answer = "";
        try {
            if (car.request == 3 || car.request == 4)
                answer = functionPointers[car.request - 3].method();
            else{
                answer = functionPointersCar[car.request].method(car);
            }
            out.writeUTF(answer);
            System.out.println(answer);
        }catch(IOException e){e.printStackTrace();}
    }

    public void run() {
        try {
            semaphore.acquire();
        } catch (Exception e) {}
        System.out.println("A client has been connected");
        functionPointers[0] = stock::get_all_cars;
        functionPointers[1] = stock::get_total;
        functionPointersCar[0]= stock::add_car;
        functionPointersCar[1]= stock::sell_car;
        functionPointersCar[2]= stock::get_make;
        try {
            ois = new ObjectInputStream(socket.getInputStream());
            out = new DataOutputStream (this.socket.getOutputStream());
            while (true){
                Car car = (Car)ois.readObject();
                if (car.request == 5)
                    break;
                complete_request(car);
            }
        } catch (IOException | ClassNotFoundException e) {System.out.println(e);}
        semaphore.release();
        System.out.println("A client has been disconnected");
    }
}

public class Server {
    final static int portNum = 2020;
    public static void main(String[] args) {
        Semaphore semaphore = new Semaphore(50, true);
        ExecutorService _threadpool = Executors.newFixedThreadPool(50);
        Stock stock = new Stock();
        try {   
            ServerSocket servesock = new ServerSocket(portNum);
            System.out.println("Server running ...");
            while (true) {
                Socket socket = servesock.accept();
                _threadpool.submit(new clientManager(socket, semaphore, stock));
            }
        } catch (IOException e) {}
     }
}