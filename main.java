import java.util.ArrayList;

//this will be the main driver class for the client side code,
//in the case of this project the client will also serve as the distributer of
//files as far as the interface goes, being able tp upload assignments on the left side of the
//interface, and download files on the right side of the interface, for rn ill design the interface
//to allow for easier interaction between attempting to upload and download files to and from the
//proxy server
public class main {

    //call interface/GUI code to allow for the program to begin
    public static void main(String[] argz) {
        ArrayList<Integer> temp = new ArrayList<Integer>();
        temp.add(0, 10);
        temp.add(1, 20);
        temp.add(2, 30);
        TestFile test = new TestFile(temp);
    }

}
