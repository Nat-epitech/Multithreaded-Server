class Car implements java.io.Serializable{
    String registration, make;
    double price, mileage;
    boolean forSale = true;
    int request;

    Car(String registration, String make, double price, double mileage, int request){
        this.registration = registration;
        this.make = make;
        this.price = price;
        this.mileage = mileage;
        this.request = request;
    }

    Car(int request){
        this.registration = null;
        this.make = null;
        this.price = 0;
        this.mileage = 0;
        this.request = request;
    }
}