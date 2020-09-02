/**
 * Created by Arnob on 4/15/2018.
 */
import java.util.*;
import java.io.*;
import java.math.*;


class Learner{

    private int my_attribute;
    private Map<Integer,Integer> my_map  = new HashMap<Integer,Integer>();
    private int [][] original_dataset = new int[21][41200];
    private int my_count;
    private Double [] my_weights = new Double[41200];
    private Double [] cumulative = new Double[41200];
    private int [][] sampled_data = new int[21][41200];
    private Double [] info_gains = new Double[20];
    private double my_error;
    private double learner_weight;


    Learner( int [][] org, int c, Double[] w )
    {
        original_dataset = org;
        my_count = c;
        my_weights = w;
        my_error=0;
        double sum = 0;

        for(int i=0;i<my_count;i++){
            sum = sum+my_weights[i];
            cumulative[i] = sum;
        }


    }

    private void sample()
    {
        Random random = new Random();
        Double d;
        int q = 0;
        int x;
        for(int i=0;i<my_count;i++){

            d = random.nextDouble();

            for(x =0;x<my_count;x++){
                if(d<cumulative[x]){
                    break;
                }
            }

            for(int p=0;p<21;p++){
                sampled_data[p][q] = original_dataset[p][x];
            }
            q++;

            //System.out.println(x);
        }

       // System.out.println(q);



    }

    private double log2(double a)
    {
        return Math.log(a)/Math.log(2);
    }

    private void info(int att){
        double inf=0;
        int pos=0;
        int neg=0;
        int c =0;
        for(int i=0;i<my_count;i++){
            if(sampled_data[20][i]==1){
                pos++;
            }
            else{
                neg++;
            }
        }
        double P  = (double)pos/(double)my_count;
        double N  = 1 - P;

        if(P>0){
            inf = (-1)*P*log2(P);
        }
        if(N>0){
            inf = inf - N*log2(N);
        }


        for(int i=1;i<=12;i++){
            c=0;
            pos = 0;
            neg = 0;

            for(int x=0;x<my_count;x++){
                if(sampled_data[att][x]==i){
                    c++;
                    if(sampled_data[20][x]==1){
                        pos++;
                    }
                    else{
                        neg++;
                    }
                }
            }

            if(c==0)continue;
            P  = (double)pos/(double)c;
            N  = 1 - P;

            double h=0;

            if(P>0){
                h = (-1)*P*log2(P);
            }
            if(N>0){
                h = h - N*log2(N);
            }


            inf = inf- ((double)c/(double)my_count)*h;


        }
        //System.out.println(inf);
        info_gains[att] = inf;

    }

    private int max_info()
    {
        double max = 0;
        int index =1;
        for(int i=0;i<20;i++){
            if(info_gains[i]>max){
                max = info_gains[i];
                index = i;
            }
        }
        return index;
    }

    private void stump()
    {
        int total_pos = 0;
        int total_neg = 0;
        for(int i=0;i<my_count;i++){
            if(sampled_data[20][i]==1){
                total_pos++;
            }
            else{
                total_neg++;
            }
        }
        int majority;
        if(total_pos>total_neg) majority=1;
        else majority = 2;


        for(int i=1;i<=12;i++){
            int c=0;
            int pos=0;
            int neg=0;
            for(int x=0;x<my_count;x++){

                if(sampled_data[my_attribute][x]==i){
                    c++;
                    if(sampled_data[20][x]==1){
                        pos++;
                    }
                    else{
                        neg++;
                    }
                }
            }

            if(c==0){
                my_map.put(i,majority);
                continue;
            }

            if(pos>neg) my_map.put(i,1);
            else my_map.put(i,2);
        }

    }

    private int validation()
    {
        int error_count =0;
        for(int i=0;i<my_count;i++){
            int a = original_dataset[my_attribute][i];

                if(my_map.get(a)!=original_dataset[20][i]){
                    error_count++;
                    my_error = my_error + my_weights[i];
                }



        }
        return error_count;

    }

    private void weight_update()
    {
        for(int x=0;x<my_count;x++){
            int a = original_dataset[my_attribute][x];
            if(my_map.get(a)==original_dataset[20][x]){
                my_weights[x] = my_weights[x] *(my_error/(1-my_error));

            }
        }

        //normalize
        Double total =0.0;
        for(int x=0;x<my_count;x++){
            total = total + my_weights[x];

        }
        for(int x=0;x<my_count;x++){
            my_weights[x] = my_weights[x]/total;
        }

        //update cumulative
        double sum = 0;
        for(int i=0;i<my_count;i++){
            sum = sum+my_weights[i];
            cumulative[i] = sum;
        }





    }



    public void learn(){
        sample();
        for(int i=0;i<20;i++){
            info(i);
        }
        my_attribute = max_info();
        stump();
        int error_count = validation();
        weight_update();

        while(error_count/my_count>0.5){

            sample();
            for(int i=0;i<20;i++){
                info(i);
            }
            my_attribute = max_info();

            stump();

            error_count = validation();
            weight_update();
        }
        //System.out.println(my_attribute);
        //System.out.println(my_map);

        double a = (1-my_error)/my_error;
        learner_weight = Math.log(a);
       // System.out.println(learner_weight);

    }

    public int get_attribute()
    {
        return my_attribute;
    }

    public Map<Integer,Integer> get_map()
    {
        return my_map;
    }

    public Double[] get_weights()
    {
        return my_weights;
    }
    public double getLearner_weight(){
        return learner_weight;
    }




}




public class AdaBoost {

    public static int[][] attributes = new int[21][41200];

    public static Double dmedians(ArrayList<Double> sorted, int cz)
    {
        int temp = (int) Math.floor( 0.50 * (cz+1));
        Double a = sorted.get(temp);
        return a;

    }

    public static int[] quantiles (ArrayList<Integer> sorted,int cz)
    {
        int [] q = new int[3];
        int a =0;
        int b =0;
        int c =0;

        int temp = (int) Math.floor( 0.25 * (cz+1));
        a = sorted.get(temp);

        temp = (int) Math.floor( 0.50 * (cz+1));
        b = sorted.get(temp);

        temp = (int) Math.floor( 0.75 * (cz+1));
        c = sorted.get(temp);

        q[0] = a;
        q[1] = b;
        q[2] = c;
        return q;
    }

    public static Double[] dquantiles(ArrayList<Double> sorted, int cz)
    {
        Double [] q = new Double[3];
        Double a =0.0;
        Double b =0.0;
        Double c =0.0;

        int temp = (int) Math.floor( 0.25 * (cz+1));
        a = sorted.get(temp);

        temp = (int) Math.floor( 0.50 * (cz+1));
        b = sorted.get(temp);

        temp = (int) Math.floor( 0.75 * (cz+1));
        c = sorted.get(temp);

        q[0] = a;
        q[1] = b;
        q[2] = c;
        return q;
    }


    public static void dataselect()
    {
        BufferedReader br = null;
        PrintWriter pw = null;
        String [] data;

        int count = 0;
        try {
            br = new BufferedReader(new FileReader( "bank-additional-full.csv" ));
            pw =  new PrintWriter(new FileWriter( "test.csv" ));

            String line;
            String temp;
            while ((line = br.readLine()) != null) {
                temp = line.replace("\"", "");
                data = temp.split(";");


                if(data[20].equals("yes")) {
                    pw.println(line);
                    count++;

                }


            }

            br.close();
            //pw.close();
        }catch (Exception e) {
            e.printStackTrace();
        }




        try {
            br = new BufferedReader(new FileReader( "bank-additional-full.csv" ));


            String line;
            String temp;
            while ((line = br.readLine()) != null) {
                temp = line.replace("\"", "");
                data = temp.split(";");
                if(data[20].equals("no") && count>0) {
                    pw.println(line);
                    count--;

                }
            }

            br.close();
            pw.close();
        }catch (Exception e) {
            e.printStackTrace();
        }

    }


    public static int dataparse()
    {

        ArrayList<Integer> ages = new ArrayList<Integer>();
        ArrayList<Integer> durs = new ArrayList<Integer>();
        ArrayList<Integer> campaigns = new ArrayList<Integer>();
        ArrayList<Integer> pdays = new ArrayList<Integer>();
        ArrayList<Integer> previous = new ArrayList<Integer>();
        ArrayList<Double> emp_var_rate = new ArrayList<Double>();
        ArrayList<Double> cons_price_index = new ArrayList<Double>();
        ArrayList<Double> confidence = new ArrayList<Double>();
        ArrayList<Double> euribor = new ArrayList<Double>();
        ArrayList<Double> employees = new ArrayList<Double>();

        String line;
        String [] data;
        File file = new File("bank-additional-full.csv");

        int count = 0;
        int pcount = 0;

        //Collecting numeric data for sort

        try(BufferedReader br = new BufferedReader(new FileReader(file))) {

            line = br.readLine();
            while ((line = br.readLine()) != null) {
                line = line.replace("\"", "");
                data = line.split(";");
                ages.add(Integer.parseInt(data[0]));
                durs.add(Integer.parseInt(data[10]));
                campaigns.add(Integer.parseInt(data[11]));
                previous.add(Integer.parseInt(data[13]));
                emp_var_rate.add(Double.parseDouble(data[15]));
                cons_price_index.add(Double.parseDouble(data[16]));
                confidence.add(Double.parseDouble(data[17]));
                euribor.add(Double.parseDouble(data[18]));
                employees.add(Double.parseDouble(data[19]));


                if(Integer.parseInt(data[12])!=999){ //We need to discard all 999s
                    pdays.add(Integer.parseInt(data[12]));
                    pcount++;
                }


                count++;
            }

            Collections.sort(ages);
            Collections.sort(durs);
            Collections.sort(campaigns);
            Collections.sort(pdays);
            Collections.sort(previous);
            Collections.sort(emp_var_rate);
            Collections.sort(cons_price_index);
            Collections.sort(confidence);
            Collections.sort(euribor);
            Collections.sort(employees);


        }catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        Map<String,Integer> job  = new HashMap<String,Integer>();
        job.put("admin.",1);
        job.put("blue-collar",2);
        job.put("entrepreneur",3);
        job.put("housemaid",4);
        job.put("management",5);
        job.put("retired",6);
        job.put("self-employed",7);
        job.put("services",8);
        job.put("student",9);
        job.put("technician",10);
        job.put("unemployed",11);
        job.put("unknown",12);


        Map<String,Integer> marital = new HashMap<String,Integer>();
        marital.put("divorced",1);
        marital.put("married",2);
        marital.put("single",3);
        marital.put("unknown",4);

        Map<String,Integer> education = new HashMap<String,Integer>();
        education.put("basic.4y",1);
        education.put("basic.6y",2);
        education.put("basic.9y",3);
        education.put("high.school",4);
        education.put("illiterate",5);
        education.put("professional.course",6);
        education.put("university.degree",7);
        education.put("unknown",8);

        Map<String,Integer> def = new HashMap<String,Integer>();
        def.put("no",1);
        def.put("yes",2);
        def.put("unknown",3);

        Map<String,Integer> housing = new HashMap<String,Integer>();
        housing.put("no", 1);
        housing.put("yes", 2);
        housing.put("unknown", 3);

        Map<String,Integer> loan = new HashMap<String,Integer>();
        loan.put("no", 1);
        loan.put("yes", 2);
        loan.put("unknown", 3);

        Map<String,Integer> contact = new HashMap<String,Integer>();
        contact.put("cellular", 1);
        contact.put("telephone", 2);

        Map<String,Integer> month  = new HashMap<String,Integer>();
        month.put("jan", 1);
        month.put("feb", 2);
        month.put("mar", 3);
        month.put("apr", 4);
        month.put("may", 5);
        month.put("jun", 6);
        month.put("jul", 7);
        month.put("aug", 8);
        month.put("sep", 9);
        month.put("oct", 10);
        month.put("nov", 11);
        month.put("dec", 12);

        Map<String,Integer> day  = new HashMap<String,Integer>();
        day.put("mon", 1);
        day.put("tue", 2);
        day.put("wed", 3);
        day.put("thu", 4);
        day.put("fri", 5);

        Map<String,Integer> poutcome  = new HashMap<String,Integer>();
        poutcome.put("failure",1);
        poutcome.put("nonexistent",2);
        poutcome.put("success",3);

        Map<String,Integer> target  = new HashMap<String,Integer>();
        target.put("yes", 1);
        target.put("no", 2);



        int [] age_quantiles = quantiles(ages,count);
        int [] dur_quantiles = quantiles(durs,count);
        int [] camp_quantiles = quantiles(campaigns,count);
        int [] pdays_quantiles = quantiles(pdays,pcount); // Pcount because we discarded all 999's
        int [] previous_quantiles = quantiles(previous,count);
        Double[] emp_quantiles = dquantiles(emp_var_rate, count);
        Double[] cons_quantiles = dquantiles(cons_price_index,count);
        Double[] confidence_quantiles = dquantiles(confidence,count);
        Double[] euribor_quantiles = dquantiles(euribor,count);
        Double[] employee_quantiles = dquantiles(employees,count);
        Double employee_median = dmedians(employees,count);





        int i = 0;
        int j = 0;
        //Collecting classes
        int test_count = 0;
        try(BufferedReader br = new BufferedReader(new FileReader(file))) {

            line = br.readLine();
            while ((line = br.readLine()) != null) {
                line = line.replace("\"", "");
                data = line.split(";");


                for(i=0;i<21;i++){
                    if(i==0){ //ages

                        if(Integer.parseInt(data[0])<=age_quantiles[0]){
                            attributes[i][j] = 1;

                        }
                        else if(Integer.parseInt(data[0])>age_quantiles[0] && Integer.parseInt(data[0])<=age_quantiles[1]){
                            attributes[i][j] = 2;

                        }
                        else if(Integer.parseInt(data[0])>age_quantiles[1] && Integer.parseInt(data[0])<=age_quantiles[2]){
                            attributes[i][j] = 3;

                        }
                        else if(Integer.parseInt(data[0])>age_quantiles[2]){
                            attributes[i][j] = 4;

                        }
                        else {
                            System.out.println(data[i]);
                        }
                    }

                    else if(i==1){//job
                        boolean a = job.containsKey(data[i]);
                        if(a==false) {
                            System.out.println(data[i]);
                        }
                        else {
                            attributes[i][j] = job.get(data[i]);
                        }

                    }

                    else if(i==2){//marital
                        boolean a = marital.containsKey(data[i]);
                        if(a==false) {
                            System.out.println(data[i]);
                        }
                        else {
                            attributes[i][j] = marital.get(data[i]);
                        }
                    }

                    else if(i==3){//education

                        boolean a = education.containsKey(data[i]);
                        if(a==false) {
                            System.out.println(data[i]);
                        }
                        else {
                            attributes[i][j] = education.get(data[i]);
                        }

                    }

                    else if(i==4){//default

                        boolean a = def.containsKey(data[i]);
                        if(a==false) {
                            System.out.println(data[i]);
                        }
                        else {
                            attributes[i][j] = def.get(data[i]);
                        }

                    }

                    else if(i==5){//housing

                        boolean a = housing.containsKey(data[i]);
                        if(a==false) {
                            System.out.println(data[i]);
                        }
                        else {
                            attributes[i][j] = housing.get(data[i]);
                        }

                    }

                    else if(i==6){//loan

                        boolean a = loan.containsKey(data[i]);
                        if(a==false) {
                            System.out.println(data[i]);
                        }
                        else {
                            attributes[i][j] = loan.get(data[i]);
                        }

                    }

                    else if(i==7){//contact

                        boolean a = contact.containsKey(data[i]);
                        if(a==false) {
                            System.out.println(data[i]);
                        }
                        else {
                            attributes[i][j] = contact.get(data[i]);
                        }

                    }

                    else if(i==8){//month

                        boolean a = month.containsKey(data[i]);
                        if(a==false) {
                            System.out.println(data[i]);
                        }
                        else {
                            attributes[i][j] = month.get(data[i]);
                        }



                    }

                    else if(i==9){//day

                        boolean a = day.containsKey(data[i]);
                        if(a==false) {
                            System.out.println(data[i]);
                        }
                        else {
                            attributes[i][j] = day.get(data[i]);
                        }

                    }

                    else if(i==10){//duration

                        if(Integer.parseInt(data[10])==0){ //special class, because always no
                            attributes[i][j] = 1;
                        }

                        else if(Integer.parseInt(data[10])>0 && Integer.parseInt(data[10])<=dur_quantiles[0]){
                            attributes[i][j] = 2;
                        }
                        else if(Integer.parseInt(data[10])>dur_quantiles[0] && Integer.parseInt(data[10])<= dur_quantiles[1]){
                            attributes[i][j] = 3;
                        }
                        else if(Integer.parseInt(data[10])>dur_quantiles[1] && Integer.parseInt(data[10])<=dur_quantiles[2]){
                            attributes[i][j] = 4;
                        }
                        else if(Integer.parseInt(data[10])>dur_quantiles[2]){
                            attributes[i][j] = 5;
                        }
                        else {
                            System.out.println(data[i]);
                        }
                    }

                    else if(i==11){//campaign
                        if(Integer.parseInt(data[11])<=camp_quantiles[0]){
                            attributes[i][j] = 1;
                        }
                        else if(Integer.parseInt(data[11])>camp_quantiles[0] && Integer.parseInt(data[11])<= camp_quantiles[1]){
                            attributes[i][j] = 2;
                        }
                        else if(Integer.parseInt(data[11])>camp_quantiles[1] && Integer.parseInt(data[11])<=camp_quantiles[2]){
                            attributes[i][j] = 3;
                        }
                        else if(Integer.parseInt(data[11])>camp_quantiles[2]){
                            attributes[i][j] = 4;
                        }
                        else {
                            System.out.println(data[i]);
                        }
                    }

                    else if(i==12){//pdays
                        int pd = Integer.parseInt(data[i]);

                        if(pd==999){ //special class
                            attributes[i][j] = 1;
                        }

                        else if(pd <=pdays_quantiles[0]){
                            attributes[i][j] = 2;
                        }
                        else if(pd > pdays_quantiles[0] && pd<= pdays_quantiles[1]){
                            attributes[i][j] = 3;
                        }
                        else if(pd>pdays_quantiles[1] && pd<=pdays_quantiles[2]){
                            attributes[i][j] = 4;
                        }
                        else if(pd>pdays_quantiles[2]){
                            attributes[i][j] = 5;
                        }
                        else {
                            System.out.println(data[i]);
                        }
                    }

                    else if(i==13){//previous

                        int p = Integer.parseInt(data[i]);

                        if(p<=previous_quantiles[0]){
                            attributes[i][j] = 1;
                        }
                        else if(p> previous_quantiles[0] && p<= previous_quantiles[1]){
                            attributes[i][j] = 2;
                        }
                        else if(p > previous_quantiles[1] && p<=previous_quantiles[2]){
                            attributes[i][j] = 3;
                        }
                        else if(p >previous_quantiles[2]){
                            attributes[i][j] = 4;
                        }
                        else {
                            System.out.println(data[i]);
                        }
                    }

                    else if(i==14){//poutcome
                        boolean a = poutcome.containsKey(data[i]);
                        if(a==false) {
                            System.out.println(data[i]);
                        }
                        else {
                            attributes[i][j] = poutcome.get(data[i]);
                        }
                    }
                    else if(i==15){ //emp var rate

                        Double e = Double.parseDouble(data[i]);

                        if(e<=emp_quantiles[0]){

                            attributes[i][j] = 1;
                        }
                        else if(e>emp_quantiles[0]&& e<=emp_quantiles[1]){

                            attributes[i][j] = 2;
                        }
                        else if(e>emp_quantiles[1] && e<=emp_quantiles[2]){

                            attributes[i][j] = 3;
                        }
                        else if(e> emp_quantiles[2]){
                            attributes[i][j] = 4;
                        }
                        else {
                            System.out.println(data[i]);
                        }

                    }
                    else if(i==16){//cons price index

                        Double c = Double.parseDouble(data[i]);

                        if(c<=cons_quantiles[0]){

                            attributes[i][j] = 1;
                        }
                        else if(c>cons_quantiles[0]&& c<=cons_quantiles[1]){

                            attributes[i][j] = 2;
                        }
                        else if(c>cons_quantiles[1] && c<=cons_quantiles[2]){

                            attributes[i][j] = 3;
                        }
                        else if(c> cons_quantiles[2]){
                            attributes[i][j] = 4;
                        }
                        else {
                            System.out.println(data[i]);
                        }


                    }
                    else if(i==17){ //cons_conf_index
                        Double cf = Double.parseDouble(data[i]);

                        if(cf<=confidence_quantiles[0]){

                            attributes[i][j] = 1;
                        }
                        else if(cf>confidence_quantiles[0]&& cf<=confidence_quantiles[1]){

                            attributes[i][j] = 2;
                        }
                        else if(cf>confidence_quantiles[1] && cf<=confidence_quantiles[2]){

                            attributes[i][j] = 3;
                        }
                        else if(cf> confidence_quantiles[2]){
                            attributes[i][j] = 4;
                        }
                        else {
                            System.out.println(data[i]);
                        }

                    }

                    else if(i==18){//euribor
                        Double e = Double.parseDouble(data[i]);

                        if(e<=euribor_quantiles[0]){

                            attributes[i][j] = 1;
                        }
                        else if(e>euribor_quantiles[0]&& e<=euribor_quantiles[1]){

                            attributes[i][j] = 2;
                        }
                        else if(e>euribor_quantiles[1] && e<=euribor_quantiles[2]){

                            attributes[i][j] = 3;
                        }
                        else if(e> euribor_quantiles[2]){
                            attributes[i][j] = 4;
                        }
                        else {
                            System.out.println(data[i]);
                        }


                    }
                    else if (i == 19) {//number of employees

                        Double n = Double.parseDouble(data[i]);
                        if(n<employee_median){
                            attributes[i][j] = 1;
                        }
                        else{
                            attributes[i][j] = 2;
                        }

                    }
                    /*else if(i==19){ //number of employees
                        Double n = Double.parseDouble(data[i]);

                        if(n<=employee_quantiles[0]){

                            attributes[i][j] = 1;
                        }
                        else if(n>employee_quantiles[0]&& n<=employee_quantiles[1]){

                            attributes[i][j] = 2;
                        }
                        else if(n>employee_quantiles[1] && n<=employee_quantiles[2]){
                            attributes[i][j] = 3;
                        }
                        else if(n> employee_quantiles[2]){
                            attributes[i][j] = 4;
                        }
                        else {
                            System.out.println(data[i]);
                        }
                    }*/
                    else if (i == 20) {
                        boolean a = target.containsKey(data[i]);
                        if (a == false) {
                            System.out.println(data[i]);
                        } else {
                            attributes[i][j] = target.get(data[i]);
                        }
                    }


                }

                j++;



            }



        }catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return count;


    }

    public static void shuffle(int c)
    {
        Random random = new Random();
        int [] temp = new int[21];

        for(int x=0;x<c*10;x++) {
            int a = (int) (random.nextDouble() * c);
            int b = (int) random.nextDouble() * c;
            if (a >= c) a--;
            if (b >= c) b--;


            for (int i = 0; i < 21; i++) { //temp = a
                temp[i] = attributes[i][a];
            }
            for (int i = 0; i < 21; i++) { //a=b
                attributes[i][a] = attributes[i][b];
            }
            for (int i = 0; i < 21; i++) {
                attributes[i][b] = temp[i];
            }
        }
    }

    public static int data_select(int c)
    {
        Random random = new Random();
        int [] temp = new int[21];

        while (c>9500){
            int a = (int) (random.nextDouble() * c);

            if(attributes[20][a]==2){

                for (int i = 0; i < 21; i++) { //temp = a
                    temp[i] = attributes[i][a];
                }
                for (int i = 0; i < 21; i++) { //a=b
                    attributes[i][a] = attributes[i][c-1];
                }
                for (int i = 0; i < 21; i++) {
                    attributes[i][c-1] = temp[i];
                }

                c--;
            }

        }

        return c;
    }



    public static void main(String[] args) {
        //dataselect();
        int c=dataparse();
        c = data_select(c);
        shuffle(c);
        System.out.println(c);



        Scanner scanner = new Scanner(System.in);
        System.out.println("how many folds?");
        int folds = scanner.nextInt();
        System.out.println("how many rounds?");
        int rounds = scanner.nextInt();

        int[][] training_data = new int[21][41200];
        int[][] test_data = new int[21][41200];
        int test_start = 0;
        int test_end = c/folds;
        int i;
        int j;
        double precision;
        double recall;
        double f1_sum=0;

        int [] predictions = new int[41200];

        for(int f=0;f<folds;f++) {

            int training_count = 0;
            int test_count =0;
            //separate test and training data
            for (j = 0; j < c; j++) {

                if (j >= test_start && j < test_end) {
                    for (int x = 0; x < 21; x++) {
                        test_data[x][test_count] = attributes[x][j];
                    }
                    test_count++;

                } else {
                    for (i = 0; i < 21; i++) {
                        training_data[i][training_count] = attributes[i][j];
                    }
                    training_count++;
                }
            }

            //Initialize weights
            Double[] w = new Double[41200];
            for (i = 0; i < training_count; i++) {

                w[i] = (double) 1 / training_count;
            }


            //multiple learners
            Learner[] learners = new Learner[50];
            for (int q = 0; q < rounds; q++) {
                learners[q] = new Learner(training_data, training_count, w);
                learners[q].learn();
                w = learners[q].get_weights();
                System.out.println(learners[q].get_attribute());
                //System.out.println(learners[q].get_map());

            }

            //System.out.println(test_count);

            //voting on test data
            for (int p = 0; p < test_count; p++) {
                double sum = 0;

                for (int q = 0; q < rounds; q++) {
                    int att = learners[q].get_attribute();

                    int att_value = test_data[att][p];

                    if (learners[q].get_map().get(att_value) == 1) {
                        sum = sum + learners[q].getLearner_weight();

                    } else {
                        sum = sum - learners[q].getLearner_weight();
                    }
                }

                if (sum >= 0) {//I said yes
                    predictions[p] = 1;

                } else { // I said no

                    predictions[p] = 2;
                }

            }

            int true_positive = 0; //say yes, actually yes
            int false_positive = 0; //say yes, actually no
            int false_negative = 0; //say no, actually yes
            int true_negative = 0;

            for(int p=0;p<test_count;p++){
                if(predictions[p]==1){
                    if(test_data[20][p]==1)true_positive++;
                    else false_positive++;
                }
                else{
                    if(test_data[20][p]==2)true_negative++;
                    else false_negative++;
                }
            }

            System.out.println("true positive "+true_positive);
            System.out.println("false positive "+false_positive);
            System.out.println("true negative "+true_negative);
            System.out.println("false negative "+false_negative);



            precision = (double) (true_positive) / (double) (true_positive + false_positive);
            recall = (double) (true_positive) / (double) (true_positive + false_negative);




            double F1;
            if(Double.isNaN(precision) && Double.isNaN(recall)){
                F1 = 1.0;

            }
            else if(Double.isNaN(precision) && !Double.isNaN(recall)){
                F1 = recall;
            }
            else if(!Double.isNaN(precision) && Double.isNaN(recall)){
                F1 = precision;
            }
            else F1 = 2.0 / ((1.0 / precision) + (1.0 / recall));
            System.out.println(F1);
            f1_sum = f1_sum + F1;

            //checking


            int temp = test_end;
            test_end = temp+c/folds;
            test_start = temp;




        }

        System.out.println("average is "+f1_sum/(double)folds);


    }
}
