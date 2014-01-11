/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package train3;

import java.awt.BorderLayout;
import java.awt.Color;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import javax.swing.JFrame;
import javax.swing.JPanel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/// checking the segment capacity.  .. and according to adjust the train's schedule. 
// ok. we will complete it in two days.
class Train3 {

    static ArrayList stationdep;
    static ArrayList stationarr;

    public static void main(String args[]) throws ParseException, FileNotFoundException, IOException {

        //System.out.println("Akash");
        String startAfter = "14:45";
        SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm");
        Date date = dateFormat.parse(startAfter);

        long startoftrain =  System.currentTimeMillis() ;

        //////////////////////////////////////////////////////////////////////////////
        ///// Filling up the All The  data structure   ///////////////
        ///////////////////////////////////////////////////////////////////////////////


    

    //    System.out.println(traindwell.toString());
        int capacity[] = new int[61];
        BufferedReader br = new BufferedReader(new FileReader("trainsch.csv"));
        BufferedReader br1 = new BufferedReader(new FileReader("timdis.csv"));
        BufferedReader br2 = new BufferedReader(new FileReader("StationCapacity.csv"));
        String capacities;
        capacities = br2.readLine();
        int capacitycount = 1;
        while ((capacities = br2.readLine()) != null) {

            StringTokenizer token = new StringTokenizer(capacities, ",");
            token.nextToken();
            capacity[capacitycount] = Integer.parseInt(token.nextToken());
            // System.out.println(capacity[capacitycount]);
            capacitycount++;


        }

        String s;

        int trains[] = new int[51];
        String timing[] = new String[51];
        int time[] = new int[51];
        String direction[] = new String[51];
        int startstation[] = new int[51];
        int endstation[] = new int[51];
        int traveltime[] = new int[61];
        float distance[] = new float[61];
        int segment[] = new int[61];
        int safetytime = 15;



        s = br.readLine();
        int counter = 1;

        while ((s = br.readLine()) != null) {
            StringTokenizer token = new StringTokenizer(s, ",");

            trains[counter] = Integer.parseInt(token.nextToken());

            timing[counter] = token.nextToken();
            direction[counter] = token.nextToken();
            StringTokenizer token1 = new StringTokenizer(token.nextToken(), " ");
            token1.nextToken();
            startstation[counter] = Integer.parseInt(token1.nextToken());
            StringTokenizer token2 = new StringTokenizer(token.nextToken(), " ");
            token2.nextToken();

            endstation[counter] = Integer.parseInt(token2.nextToken());

            Date datee;
            datee = dateFormat.parse(timing[counter]);


            int minutes = (60 * datee.getHours()) + datee.getMinutes();
            time[counter] = minutes;

            counter++;
        }

        //// Deal with second file.

        String string = br1.readLine();
        int counter1 = 1;
        while ((string = br1.readLine()) != null) {

            StringTokenizer token = new StringTokenizer(string, ",");
            segment[counter1] = Integer.parseInt(token.nextToken());
            token.nextToken();
            token.nextToken();
            traveltime[counter1] = Integer.parseInt(token.nextToken());
            distance[counter1] = Float.parseFloat(token.nextToken());
            counter1++;
        }


        /////////////////////////////////////////////////////////////////////////////////////////// End of filling up the data structure 


        ///////////////////////////////////////////////////////////////////////////////////////////

        /////////////////////////////// Scheduling the first train  with including dwelling time //

        ///////////////////////////////////////////////////////////////////////////////////////////

        HashMap stationwaiting = new HashMap();  // key = station , value = list (train no , direction , Waiting time)
        HashMap trainwaiting = new HashMap();
        int temparrival = 0;

        HashMap finalstationwaiting = new HashMap();

        stationdep = new ArrayList();  /// kind of three dimension.
        HashMap sd = new HashMap();
        stationarr = new ArrayList();
        HashMap sa = new HashMap();

        if (direction[1].equals("E")) {


            System.out.println("This is EastBound");
            HashMap expecteddepart = new HashMap();
            HashMap expectedarrival = new HashMap();
            int actualdep = time[1];
            int pplus = 0;
            for (int station = 1; station < 60; station++) {


                pplus = actualdep + traveltime[station];

                expecteddepart.put(station, actualdep);
                expectedarrival.put(station + 1, pplus);
                expectedarrival.put(station, actualdep);


            }
            stationdep.add(expecteddepart);
            stationarr.add(expectedarrival);



        } else {

            System.out.println("This is WestwestBound");
            HashMap expecteddepart = new HashMap();
            HashMap expectedarrival = new HashMap();
            int actualdep = time[1];

            int pplus = 0;
            for (int station = 60; station > 1; station--) {

                if (station == 60) {
                    //actualdep = actualdep + 60
                    pplus = actualdep + traveltime[station];

                } else {

                        actualdep = (int) expectedarrival.get(station);
                        pplus = actualdep + traveltime[station];
                    
                }

                expecteddepart.put(station, actualdep);
                expectedarrival.put(station - 1, pplus);
                if (station == 60) {
                    expectedarrival.put(station, actualdep);
                }


            }

            stationdep.add(expecteddepart);
            stationarr.add(expectedarrival);


        }

        /////////////////////////////////////////////////////////////////////////////////////////////////////////////

        ////////      Scheduling all the 50 trains     ///////////////////////////////  

        //////////////////////////////////////////////////////////////////////////////////////////////////////////////   

        // - first loop iterate through all the trains
        // - second loop iterate through all station
        // - for each station we check current trains with the previously schedule the trains
        //          case 1- if at current station capacity is not exceeded then schedule the train
        //          case 2- if capacity exceeds, go to previous station and adjust the schedule ///
        //          note - for giving the impression of recursion we use little trick which i have shown below.
        // - here in this program we schedule the train including Dwelling time.

        /////////////////////////////////////////////////////////////////////////////////////////////////////////////  

        for (int j = 2; j < 51; j++) {

            //  checking the direction of trains.


            if (direction[j].equals("E")) {

                System.out.println("Hey This is EastBound");
                HashMap expecteddepart = new HashMap();
                HashMap expectedarrival = new HashMap();

                // it is loop which iterate through all the stations
                for (int start = startstation[j]; start < endstation[j]; start++) {


                    //  intitialize the departure time of the current train.

                    int Actualdeptime;
                    if (start == startstation[j]) {

                        Actualdeptime = time[j];
                        temparrival = time[j];

                    } else {



                            if (expecteddepart.get(start) != null) {
                                Actualdeptime = (int) expecteddepart.get(start);
                                temparrival = (int) expectedarrival.get(start);
                            } else {

                                Actualdeptime = (int) expectedarrival.get(start);
                                temparrival = Actualdeptime;
                            }
                        

                    }



                    // Compare with all the previous trains and consider two cases if capacity exceeded then reschedule the train
                    // otherwise schedule normally.




                    for (int previous = 1; previous < j; previous++) {

                        if (direction[previous].equals("W")) {

                            int timee;
                            int pplus1 = 0;
                            timee = traveltime[start];
                            pplus1 = Actualdeptime + timee;
                            HashMap map = (HashMap) stationdep.get(previous - 1);
                            int value = 0;
                            if (map.get((start + 1)) != null) {

                                value = (int) map.get((start + 1));

                            } else {

                                value = 0;

                            }

                            HashMap map1 = (HashMap) stationarr.get(previous - 1);

                            int value1 = 0;

                            if (map1.get(start) != null) {

                                value1 = (int) map1.get(start);

                            } else {

                                value1 = 0;
                            }

                            int positive = (int) ((value - pplus1) * (value1 - Actualdeptime));

                            // Checking for the collision. 


                            if (positive < 0) {

                                System.out.println("collision At:" + start + "With train" + previous);

                                //////// ADD THIS STRINGS ///////    

                                String wait = temparrival + "," + value1;
                                Actualdeptime = value1;

                                if (stationwaiting.get(start) != null) {
                                    HashMap www = (HashMap) stationwaiting.get(start);    /// (station, hashmap(train, waitingtime))

                                    if (www.get(j) != null) {
                                        www.put(j, wait);
                                        stationwaiting.put(start, www);

                                    } else {
                                        www.put(j, wait);
                                        stationwaiting.put(start, www);


                                    }

                                } else {

                                    trainwaiting.put(j, wait);
                                    stationwaiting.put(start, trainwaiting);

                                }
                            } else {  /// if no collision occure.

                                Actualdeptime = Actualdeptime;

                            }


                        } ///  if both trains are on the same side.
                        else {

                            /// Check the trains which are going into same direction the only we need to care about is safety distance
                            // so , check for the safety distance and according to adjust the train.
                            // Safety distance is 15 minutes.


                            HashMap map = (HashMap) stationdep.get(previous - 1);
                            int value;
                            int capacity1 = capacity[start];
                            if (map.get(start) != null) {

                                value = (int) map.get(start);
                            } else {
                                value = 0;

                            }
                            HashMap map1 = (HashMap) stationarr.get(previous - 1);
                            int value1 = 0;

                            if (map1.get((start + 1)) != null) {

                                value1 = (int) map1.get((start + 1));;

                            } else {

                                value1 = 0;

                            }

                            int pplus1 = traveltime[start];
                            if (Actualdeptime < value + safetytime) {
                                Actualdeptime = value + safetytime;
                                // (Add delay because of capacity exceed or same direction so mantain safety distance.) 
                                pplus1 = Actualdeptime + pplus1;
                                String wait = temparrival + "," + pplus1;
                                HashMap temporary = new HashMap();
                                temporary.put(j, wait);
                                stationwaiting.put(start, temporary);

                            }

                        }
                    }

                    ///  all the trains per segment
                    expecteddepart.put(start, Actualdeptime);
                    int pplus1 = traveltime[start] + Actualdeptime;

                    expectedarrival.put(start + 1, pplus1);

                    int testwait = Actualdeptime - temparrival;

                    if (testwait > 0) {
                        String store = temparrival + "," + Actualdeptime;

                        if (finalstationwaiting.get(start) == null) {
                            HashMap mappings = new HashMap();
                            mappings.put(j, store);
                            finalstationwaiting.put(start, mappings);


                        } else {

                            HashMap finalhashmap = (HashMap) finalstationwaiting.get(start);
                            finalhashmap.put(j, store);
                            finalstationwaiting.put(start, finalhashmap);

                        }


                    }

                    // Checking that capacity has been exceeded or not.
                    HashMap tempcapacitycalculate = new HashMap();
                    String trainstring = "";
                    int countingcapacity = 0;
                    if (finalstationwaiting.get(start) != null) {
                        countingcapacity = 1;
                        HashMap capacitycheck = (HashMap) finalstationwaiting.get(start);
                        // capacitycheck.
                        trainstring = "";
                        Iterator it = capacitycheck.keySet().iterator();

                        while (it.hasNext()) {

                            int trainno = (int) it.next();
                            //System.out.println(trainno+"String :::"+capacitycheck.get(trainno));

                            String stringfortoken = (String) capacitycheck.get(trainno);
                            StringTokenizer tokens = new StringTokenizer(stringfortoken, ",");
                            int starttime = Integer.parseInt(tokens.nextToken());
                            int endtime = Integer.parseInt(tokens.nextToken());

                            if (trainno != j) {
                                if (endtime > temparrival && endtime < Actualdeptime) {

                                    trainstring = trainstring + trainno;
                                    countingcapacity++;
                                    tempcapacitycalculate.put(starttime, endtime);
                                    //          System.out.println("---------------------------------------------------"+starttime+""+endtime);
                                }
                            }
                        }

                    }


                    if (tempcapacitycalculate.size() != 0) {
                        //     System.out.println("------------------"+tempcapacitycalculate.toString());
                    }

                    HashMap compare = new HashMap();
                    compare = tempcapacitycalculate;

                    //    System.out.println("CCOMMMPARRE"+compare.toString());
                    Iterator iterate = tempcapacitycalculate.keySet().iterator();
                    Iterator iterate1 = compare.keySet().iterator();
                    int startingtime;
                    int endingtime;
                    int Maxcapacity = 0;

                    while (iterate.hasNext()) {
                        int capacitychecking = 0;
                        startingtime = (int) iterate.next();
                        endingtime = (int) tempcapacitycalculate.get(startingtime);
                        while (iterate1.hasNext()) {

                            //      System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>..");

                            int starttime = (int) iterate1.next();
                            int endtime = (int) compare.get(starttime);

                            //          System.out.println("starttime:"+starttime+"End time"+endtime);
                            if (starttime != startingtime) {


                                if ((startingtime > starttime && (endingtime > endtime || endingtime < endtime)) || (starttime > startingtime) && (endtime > endingtime || endtime < endingtime)) {

                                    capacitychecking++;

                                }


                            }

                        }

                        if (capacitychecking > Maxcapacity) {

                            Maxcapacity = capacitychecking;
                        }

                    }



                    countingcapacity = Maxcapacity + 1;









                    //  Adjust the schedule because capacity has been exceeded .

                    if (capacity[start] < countingcapacity) {

                        System.out.println("Counting capacity :::" + countingcapacity);


                        System.out.println("Capacity Exceed at station" + start + "Capacity is " + capacity[start] + "but final is" + countingcapacity);
                        System.out.println("Waited trains" + trainstring + "And current train:" + j);

                        int changetime = 0 ;
                        if(expecteddepart.get(start - 1) != null ){
                          changetime = (int) expecteddepart.get(start - 1);
                        }else{
                         
                         System.out.println("The problem With Given Data");
                       }
                       
                        changetime = changetime + testwait;
                        expecteddepart.put(start - 1, changetime);

                        expecteddepart.remove(start);
                        expectedarrival.remove(start + 1);

                        // Removing the entry from hashmap

                        HashMap removeentry = (HashMap) finalstationwaiting.get(start);
                        removeentry.remove(j);
                        finalstationwaiting.put(start, removeentry);

                        // important -- 
                        // This statement give the impression of the recursion

                        start = start - 2;

                    }
                }
                //// Save all the departure and arrival time at each station in the global arraylist

                stationdep.add(expecteddepart);
                stationarr.add(expectedarrival);


            } else {


                System.out.println("This is WestBound");
                HashMap expecteddepart = new HashMap();
                HashMap expectedarrival = new HashMap();

                //// Same loops as above but for the Westbounds

                for (int start = startstation[j]; start >= endstation[j]; start--) {

                    int Actualdeptime;
                    if (start == startstation[j]) {

                        Actualdeptime = time[j];
                        temparrival = Actualdeptime;
                        
                    } else {

                      

                            if (expecteddepart.get(start) != null) {
                                Actualdeptime = (int) expecteddepart.get(start);
                                temparrival = (int) expectedarrival.get(start);
                            } else {
                                Actualdeptime = (int) expectedarrival.get(start);
                                temparrival = Actualdeptime;
                            }
                        
                    }
                    //// All the previosu trains according to which i have to adjust the schedule
                    for (int previous = 1; previous < j; previous++) {

                        if (direction[previous].equals("E")) {

                            int timee;
                            int pplus1 = 0;
                            timee = traveltime[start];
                            pplus1 = Actualdeptime + timee;
                            HashMap map = (HashMap) stationdep.get(previous - 1);
                            int value = 0;
                            if (map.get((start - 1)) != null) {
                                value = (int) map.get((start - 1));
                            } else {

                                value = 0;
                            }

                            HashMap map1 = (HashMap) stationarr.get(previous - 1);
                            int value1 = 0;
                            if (map1.get(start) != null) {

                                value1 = (int) map1.get(start);

                            } else {

                                value1 = 0;

                            }

                            // checking for the collision
                            int positive = (int) ((value - pplus1) * (value1 - Actualdeptime));


                            if (positive < 0) {


                                // collision occure here
                                Actualdeptime = value1;

                                System.out.println("collision At:" + start + "With train" + previous);

                                //////// ADD THIS STRINGS ///////    
                                String wait = temparrival + "," + value1;
                                Actualdeptime = value1;


                                if (stationwaiting.get(start) != null) {
                                    HashMap www = (HashMap) stationwaiting.get(start);    /// (station, hashmap(train, waitingtime))

                                    if (www.get(j) != null) {
                                        www.put(j, wait);
                                        stationwaiting.put(start, www);

                                    } else {
                                        www.put(j, wait);
                                        stationwaiting.put(start, www);


                                    }



                                } else {
                                    trainwaiting.put(j, wait);
                                    stationwaiting.put(start, trainwaiting);

                                }

                            } else {  /// if no collision occure.

                                Actualdeptime = Actualdeptime;

                            }


                        } /// This loop is if both the trains are on the same side.
                        else {
                            // if both the trains are on the same direction do somethig useful.
                            //   match the time of previous trains    
                            //  Maintain the safety distance and the take care about the capacity

                            int currentcapacity = capacity[start];
                            HashMap map = (HashMap) stationdep.get(previous - 1);
                            int value;

                            if (map.get(start) != null) {

                                value = (int) map.get(start);

                            } else {

                                value = 0;
                            }
                            HashMap map1 = (HashMap) stationarr.get(previous - 1);
                            int value1 = 0;

                            if (map1.get((start - 1)) != null) {

                                value1 = (int) map1.get((start - 1));

                            } else {

                                value1 = 0;

                            }

                            // Adjust the schedule if both the trains are on the same side but they are not far apart than minimum safety distance.

                            int pplus1 = traveltime[start];
                            if (Actualdeptime < value + safetytime) {
                                Actualdeptime = value + safetytime;
                                // (this type of value) 
                                pplus1 = Actualdeptime + pplus1;
                                HashMap temporary = new HashMap();
                                String wait = temparrival + "," + pplus1;
                                temporary.put(j, wait);
                                stationwaiting.put(start, temporary);
                            }

                        }
                    }

                    /// Adjustment the departure or arrival time of current train
                    expecteddepart.put(start, Actualdeptime);
                    int pplus1 = traveltime[start] + Actualdeptime;

                    expectedarrival.put(start - 1, pplus1);

                    int testwait = Actualdeptime - temparrival;
                    if (testwait > 0) {

                        String store = temparrival + "," + Actualdeptime;
                        if (finalstationwaiting.get(start) == null) {

                            HashMap mappings = new HashMap();

                            mappings.put(j, store);
                            finalstationwaiting.put(start, mappings);
                        } else {

                            HashMap finalhashmap = (HashMap) finalstationwaiting.get(start);
                            finalhashmap.put(j, store);
                            finalstationwaiting.put(start, finalhashmap);

                        }

                    }


                    HashMap tempcapacitycalculate = new HashMap();
                    String trainwaited = "";
                    int countingcapacity = 0;
                    if (finalstationwaiting.get(start) != null) {
                        countingcapacity = 1;
                        HashMap capacitycheck = (HashMap) finalstationwaiting.get(start);
                        // capacitycheck.
                        trainwaited = "";
                        Iterator it = capacitycheck.keySet().iterator();
                        while (it.hasNext()) {

                            int trainno = (int) it.next();
                            //System.out.println(trainno+"String :::"+capacitycheck.get(trainno));

                            String stringfortoken = (String) capacitycheck.get(trainno);
                            StringTokenizer tokens = new StringTokenizer(stringfortoken, ",");
                            int starttime = Integer.parseInt(tokens.nextToken());
                            int endtime = Integer.parseInt(tokens.nextToken());

                            ////  Now we 


                            /*
                                 
                             for(int cou = 0 ; cou < capacitycheck.size(); cout++){
                             *  
                             *    
                             * 
                             * 
                             * 
                             * 
                             * }
                                 
                                 
                                 
                                 
                                 
                                 
                             */
                            if (trainno == 38) {
                                System.out.println("temp ariival " + temparrival + "Actial departure:" + Actualdeptime);
                            }

                            if (trainno != j) {
                                if (endtime > temparrival && endtime < Actualdeptime) {


                                    trainwaited = trainwaited + trainno;
                                    countingcapacity++;

                                    tempcapacitycalculate.put(starttime, endtime);
                                }

                            }

                        }

                    }






                    HashMap compare = new HashMap();
                    compare = tempcapacitycalculate;
                    Iterator iterate = tempcapacitycalculate.keySet().iterator();
                    Iterator iterate1 = compare.keySet().iterator();
                    int startingtime;
                    int endingtime;
                    int Maxcapacity = 0;

                    while (iterate.hasNext()) {
                        int capacitychecking = 0;
                        startingtime = (int) iterate.next();
                        endingtime = (int) tempcapacitycalculate.get(startingtime);
                        while (iterate1.hasNext()) {

                            int starttime = (int) iterate1.next();
                            int endtime = (int) compare.get(starttime);

                            if (starttime != startingtime) {

                                if ((startingtime > starttime && (endingtime > endtime || endingtime < endtime)) || (starttime > startingtime) && (endtime > endingtime || endtime < endingtime)) {

                                    capacitychecking++;

                                    // System.out.println();

                                }


                            }

                        }

                        if (capacitychecking > Maxcapacity) {

                            Maxcapacity = capacitychecking;
                        }

                    }



                    countingcapacity = Maxcapacity + 1;










                    //  capacity has been exceeded so, reschedule the schedule and go back on previous station

                    if (capacity[start] < countingcapacity) {
                        System.out.println("Capacity Exceed at station" + start + "Capacity is " + capacity[start] + "but final is" + countingcapacity);
                        System.out.println("Trains waited are : " + trainwaited + "And current train:" + j);
                        System.out.println("Counting capacity :::" + countingcapacity);


                        /// Ajusting the capacity in the above station


                        System.out.println("Expected deprt. ------ " + expecteddepart.toString() + "station " + start);
                        System.out.println("Expected arrival. ------ " + expectedarrival.toString() + "station " + start);
                       
                         int changetime = 0 ;
                        if (expecteddepart.get(start + 1) != null ){
                         
                            changetime = (int) expecteddepart.get(start + 1);
                        }else {
                        
                          System.out.println(" -----  Problem With The given Data ..... ---------");
                        }
                        
                     
                        
                        changetime = changetime + testwait;
                        expecteddepart.put(start + 1, changetime);
                        HashMap removeentry = (HashMap) finalstationwaiting.get(start);
                        removeentry.remove(j);
                        finalstationwaiting.put(start, removeentry);

                        // We are going back to previous station so we need to remove the entry from 

                        expecteddepart.remove(start);
                        expectedarrival.remove(start - 1);

                        // This statement give the impression of recursion. 

                        start = start + 2;



                    }

                }  // for loop

                stationdep.add(expecteddepart);
                stationarr.add(expectedarrival);
            } /// done with eastbound trains.


            //  System.out.println("Final Station Waiting:"+finalstationwaiting.toString());

        }

        // Calculating the Average time. 
    
         ArrayList averagetrain = new ArrayList();
         ArrayList averagetraveltime = new ArrayList();
         for(int train = 1 ; train < 51 ; train++){
         
         int start = startstation[train];
               
         int end   = endstation[train];
             
         HashMap training = (HashMap) stationdep.get(train-1);
            
         int startingtime ;
         int endingtime ;
         float finalavg;
         float dist = 0 ;
                        
         if((direction[train]).equals("W")){
                   
         startingtime = (int) training.get(start);
         endingtime = (int) training.get(end+1) ;
                  
         for(int avg = start ; avg > end ; avg--){
                    
         dist =  (dist + distance[avg]) ;
                   
         }
                   
         finalavg = dist /(endingtime- startingtime)  ;  
         averagetraveltime.add(endingtime-startingtime);
         averagetrain.add(finalavg);
                   
         }else{
                   
         startingtime = (int) training.get(start);
         endingtime = (int) training.get(end-1) ;
               
         for(int avg = start ; avg <end ; avg++){
                    
         dist =  (dist + distance[avg]) ;
                   
         }
               
         finalavg = dist /(endingtime- startingtime)  ; 
         averagetraveltime.add(endingtime-startingtime);
         averagetrain.add(finalavg); 
                 
         }
               
         }
         /// End to End Travelling time
         
         
         int finalendtoend = 0 ;
         for(int i = 1 ; i < 51 ;i ++){
        
          System.out.println("End to End Travel Time of train "+i+" "+averagetraveltime.get(i-1) +"  minutes ");
          finalendtoend = finalendtoend + (int)averagetraveltime.get(i-1);
         }
         
         finalendtoend = finalendtoend/ 50 ;
         
         System.out.println("For 50 trains Average Traveltime = "+finalendtoend+"  minutes.");
         
         float finalavg = 0; 
         for(int i = 1 ; i < 51 ;i++){
                 //  System.out.println("Speed of train "+i+" "+averagetrain.get(i-1));
         finalavg = finalavg + (float) averagetrain.get(i-1);
                     
         }
       
         finalavg = finalavg/50
         ;
 
         System.out.println("Final averagespeed for all trains "+ finalavg);
               
       
                
         //// Average earliness and tardiness computation  /////
   
         ArrayList tardiness = new ArrayList();
         for(int i = 1 ; i < 51 ; i++){
                
         int start = startstation[i];
         int end = endstation[i] ;
                   
         int finaltraveltime = 0 ;
         if(direction[i].equals("W")){
         // finaltraveltime = time[i] ;
         //    System.out.println("travel"+ finaltraveltime);
         for(int starting = start ; starting >end ; starting --){
                      
                         
         finaltraveltime = finaltraveltime + traveltime[starting];
                     
                     
         }
         int calculation = (int) averagetraveltime.get(i-1);
      //   System.out.println("Calculation:"+calculation+"finaltraveltime"+finaltraveltime);
         tardiness.add(calculation-finaltraveltime);
                     
         }else{
                    
         // finaltraveltime = time[i] ;
         for(int starting = start ; starting <end ; starting ++){
         finaltraveltime = finaltraveltime + traveltime[starting];
                     
         }
                     
         int calculation = (int) averagetraveltime.get(i-1);
        // System.out.println("Calculation:"+calculation+"finaltraveltime"+finaltraveltime);
        
         tardiness.add(calculation-finaltraveltime);
                    
         }
                
         }
                
         int finaltardi = 0 ;
         for(int i = 1 ; i <= tardiness.size() ; i++){
         finaltardi = finaltardi + (int)tardiness.get(i-1);
         
         System.out.println("Tardiness of train "+ i + " " + tardiness.get(i-1) );
         
         }
         System.out.println("For 50 trains Average Tardiness "+(finaltardi/50) );
                
                  
                
         //// Writing the data to computing file.        
            
            
         FileWriter fw = new FileWriter("Computing time.csv") ;
         BufferedWriter bw = new BufferedWriter(fw);   
         bw.write("stations");
         bw.write(",");
         for(int i = 1 ; i <60 ; i++)
         bw.write("station:"+i+",");
               
         bw.write("\n");
           
         for(int trainnumber = 1 ;trainnumber <51 ; trainnumber++ ){
 
         bw.write("Train:"+trainnumber+",");
         HashMap number =(HashMap) stationdep.get(trainnumber-1);
         for(int i = 1 ; i < 60 ;i++){
                  
         if(number.containsKey(i)){
                        
         bw.write(number.get(i)+",");
                   
         }else{
                   
         bw.write(0+",");
         }
                 
                 
         }
           
         bw.write("\n");
           
           
         } 
         //             */
          bw.close();

        ///// Ploting the graphs

          long endoftrain =  System.currentTimeMillis() ;

          long computingtime = endoftrain - startoftrain ;
          
          System.out.println("Computing Time - "+computingtime + "milisecond ");
          
          
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel panel = new JPanel();

        frame.add(panel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.show();
        //  frame.setSize(650, 650);
        XYPlot plot;
        String yAxis = "stations";
        String xAxis = "time in minutes.";
        //  XYSeriesCollection dataset = createDataset("Series");
        XYSeriesCollection dataset1 = new XYSeriesCollection();


        XYSeries series = new XYSeries("Series");
        dataset1.addSeries(series);
        JFreeChart chart = ChartFactory.createXYLineChart("", xAxis, yAxis,
                dataset1, PlotOrientation.VERTICAL, false, false, false);
        chart.setBackgroundPaint(Color.white);
        plot = chart.getXYPlot();
        plot.setBackgroundPaint(Color.lightGray);
        plot.setDomainGridlinePaint(Color.white);
        plot.setRangeGridlinePaint(Color.white);
        ValueAxis axis = plot.getDomainAxis();
        axis.setAutoRange(true);
        NumberAxis rangeAxis2 = new NumberAxis("Range Axis 2");
        rangeAxis2.setAutoRangeIncludesZero(false);
        JPanel content = new JPanel(new BorderLayout());
        ChartPanel chartPanel;
        chartPanel = new ChartPanel(chart);
        content.add(chartPanel);
        panel.add(chartPanel);

        List<XYSeriesCollection> seriesArrayList = new ArrayList<XYSeriesCollection>();

        for (int i = 1; i < 51; i++) {
            XYSeries xy = new XYSeries("data" + i);
            HashMap mapping = (HashMap) stationdep.get(i - 1);

            HashMap mappp = (HashMap) stationarr.get(i - 1);
            XYSeriesCollection dataset2 = new XYSeriesCollection(xy);

            for (int j = 1; j <= 60; j++) {
                if (j <= 60) {
                    if (mapping.containsKey(j)) {

                        xy.add((int) mapping.get(j), j);
                        if (mappp.containsKey(j)) {
                            xy.add((int) mappp.get(j), j);
                        }


                    } else {
                    }
                }
            }

            plot.setDataset(i, dataset2);
            plot.setRenderer(i, new StandardXYItemRenderer());

            seriesArrayList.add(dataset2);

        }


    }
}