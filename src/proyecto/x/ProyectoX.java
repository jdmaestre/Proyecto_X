/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

// update Posiciones set pts = 0, pj=0, pjl=0, pjv=0, pg=0, pgl=0, pgv=0, pe=0, pel=0, pev=0, pp=0, ppl=0, ppv=0, gf=0, gc=0, gd=0;
package proyecto.x;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JTextField;


/**
 *
 * @author Jose
 */
public class ProyectoX {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here       
       
        
        JFrame form = new JFrame();
        form.setVisible(true);        
        
        //simularTemporada();        
        
        
    }
    
public void simularTemporada (String liga, double criterioPME, JLabel totalAcumulado, JLabel NumApuestas, JTextArea output ){
    
        Connection connection = null;
        Metodos_Simulacion ms = new Metodos_Simulacion();
        double acumulado = Double.valueOf(totalAcumulado.getText().replaceAll(",","."));
        int acumuladoApuestas = Integer.parseInt(NumApuestas.getText());
        
        try
        {
          double Cuenta = 0;
          double ROI = 0;
          int Avalidas = 0;
          int aux = 0; 
            
          // create a database connection          
          connection = DriverManager.getConnection("jdbc:sqlite:BdPrueba.sqlite");
          Statement statement = connection.createStatement();  
          statement.setQueryTimeout(30);  // set timeout to 30 sec.
          Statement statement2 = connection.createStatement();  
          statement2.setQueryTimeout(30);  // set timeout to 30 sec.
                     
          //Crear tabla de posiciones
          ResultSet rs = statement.executeQuery("select * from " + liga);
       
          statement2.executeUpdate("drop table if exists Posiciones");
          statement2.executeUpdate("CREATE TABLE \"Posiciones\" (\"equipo\" , \"pj\" INTEGER NOT NULL DEFAULT 0,"
                  + " \"pjl\" INTEGER DEFAULT 0, \"pjv\" INTEGER DEFAULT 0, \"pg\" , \"pgl\" , \"pgv\" , \"pe\" ,"
                  + " \"pel\" , \"pev\" , \"pp\" , \"ppl\" , \"ppv\" , \"gf\" , \"gc\" , \"gd\" , \"pts\" )");
          
          String[] Array = new String[20];
          int i = 0;
          
          while (i < 20) {
              int sw = 0 ;                
              String equipoL = rs.getString("HomeTeam");
              for (int j = 0; j < 20; j++) {
                  
                  if (equipoL.equals(Array[j]))  {
                      
                      sw = 1;                        
                  }
                  
              }
              if (sw == 0) {
                  
                  Array[i] = equipoL; 
                  i = i + 1;
              }
              rs.next();
              
                
          }
          
            for (i = 0; i < 20; i++) {
                statement2.executeUpdate("INSERT INTO Posiciones (equipo) VALUES  ('" + Array[i] +"');");
                
            }
           
            
          rs = statement.executeQuery("select * from " + liga);           
          while(rs.next())
          {
            // read the result set
            //System.out.println("name = " + rs.getString("name"));
            //System.out.println("id = " + rs.getInt("gL"));
            String equipoL = rs.getString("HomeTeam");
            String equipoV = rs.getString("AwayTeam");
            String resultado = rs.getString("FTR");
            double cuotaL = rs.getDouble("B365H");
            double cuotaX = rs.getDouble("B365D");
            double cuotaV = rs.getDouble("B365A");
            double gapPME = criterioPME;
            int gL = rs.getInt("FTHG");
            int gV = rs.getInt("FTAG");         
            System.out.println("Patido # "+String.valueOf(rs.getRow()));            
            System.out.println("Partido:" + equipoL + " - " + equipoV );                            
            System.out.printf("CuotaH: %.2f |", cuotaL);
            System.out.printf("CuotaX: %.2f |", cuotaX);
            System.out.printf("CuotaA: %.2f |%n", cuotaV);
            
            //Encontrar apuestas EV+ de los partidos
            String Bet = ms.metodoBasico(equipoL, equipoV, cuotaL, cuotaX, cuotaV, gapPME, connection);
            
              if (Bet.equals("H") || Bet.equals("X") || Bet.equals("A")) {
                  
                  Avalidas = Avalidas +1;
                                    
              }
              System.out.println("Apuestas realizadas: " + String.valueOf(Avalidas));
              NumApuestas.setText(String.valueOf(Avalidas  + acumuladoApuestas));
                                    
            //"Jugar el partido"
            jugarPartido(equipoL, equipoV, gL, gV, connection);
            
           
            //Realizar apuesta
            Cuenta = realizarApuesta(resultado, cuotaL, cuotaX, cuotaV, Bet, Cuenta, ROI, aux);
            System.out.println(String.valueOf("Dinero :" + (Cuenta) + "$"));
            totalAcumulado.setText(String.format("%.2f", Cuenta + acumulado));
            System.out.println(" ");
            
          }
          
          statement2.executeUpdate("DROP TABLE Posiciones");
          
        }
        catch(SQLException e)
        {
          // if the error message is "out of memory", 
          // it probably means no database file is found
          System.err.println(e.getMessage());
          System.err.println(e);
          
        }
        finally
        {
          try
          {
            if(connection != null)
              connection.close();
          }
          catch(SQLException e)
          {
            // connection close failed.
            System.err.println(e);
          }
        }
    }
    

    
public static void jugarPartido(String equipoL, String equipoV, int gL,
                                        int gV, Connection connection){
        
           
        try{
        
            Statement statement2 = connection.createStatement();
            Statement statement3 = connection.createStatement();
            statement2.setQueryTimeout(30);  // set timeout to 30 sec.
            statement3.setQueryTimeout(30);  // set timeout to 30 sec.
            ResultSet rsP = statement2.executeQuery("select * from Posiciones");
              if (gL > gV) {
                  //System.out.println("Local");
                  //------------------------Codigo si gana Local--------------------------------
                  while (rsP.next()){
                      
                      if (rsP.getString("equipo").equals(equipoL) ) {
                        // Puntos  
                        int pts = rsP.getInt("pts"); pts = pts + 3;                 
                                                
                        //Partidos jugados
                        int pj = rsP.getInt("pj"); pj = pj +1;                                                
                        //Partidos jugados
                        int pjl = rsP.getInt("pjl"); pjl = pjl +1;                                                
                        //Partidos ganados 
                        int pg = rsP.getInt("pg"); pg = pg +1;                                           
                        //Partidos ganados como local
                        int pgl = rsP.getInt("pgl"); pgl = pgl +1;                                                
                        //Goles
                            //Goles a favor
                        int gf = rsP.getInt("gf"); gf = gf + gL;
                            //Goles en contra
                        int gc = rsP.getInt("gc"); gc = gc + gV;
                            //Gol diferencia
                        int gd = rsP.getInt("gd"); gd = gd + (gL - gV);
                        
                        statement3.executeUpdate("update Posiciones set pts = " + pts +
                                                    ", pj = "+ pj +
                                                    ", pjl = "+ pjl +
                                                    ", pg = "+ pg +
                                                    ", pgl = "+ pgl +
                                                    ", gf = "+ gf +
                                                    ", gc = "+ gc +
                                                    ", gd = "+ gd +                                
                                                    " where equipo = '"+ equipoL +"' ;");
                        
                      }
                      
                      if (rsP.getString("equipo").equals(equipoV)) {
                          
                        // Partidos jugados 
                        int pj = rsP.getInt("pj"); pj = pj + 1;                     
                                                
                        //Partidos jugados como visitante
                        int pjv = rsP.getInt("pjv"); pjv = pjv + 1;
                        
                        // Partidos Perdidos
                        int pp = rsP.getInt("pp"); pp = pp + 1;         
                        // Partidos Perdidos como visitante
                        int ppv = rsP.getInt("ppv"); ppv = ppv + 1;               
                        //Goles
                            //Goles a favor
                        int gf = rsP.getInt("gf"); gf = gf + gV;                      
                            //Goles en contra
                        int gc = rsP.getInt("gc"); gc = gc + gL;                        
                            //Gol diferencia
                        int gd = rsP.getInt("gd"); gd = gd + (gV - gL);                        
                        
                        statement3.executeUpdate("update Posiciones set pj = " + pj +                                                    
                                                    ", pjv = "+ pjv +
                                                    ", pp = "+ pp +
                                                    ", ppv = "+ ppv +
                                                    ", gf = "+ gf +
                                                    ", gc = "+ gc +
                                                    ", gd = "+ gd +                                
                                                    " where equipo = '"+ equipoV +"' ; ");
                          
                      }                    
                                           
                      
                  }                       
                  
              }else{
                  if (gL == gV) {
                      //System.out.println("Empate");
                      //---------------------------Codigo si empatan------------------------------
                      
                      while (rsP.next()){
                        if (rsP.getString("equipo").equals(equipoL) ) {
                        // Puntos  
                        int pts = rsP.getInt("pts"); pts = pts + 1;                      
                                                
                        //Partidos jugados
                        int pj = rsP.getInt("pj"); pj = pj +1;
                                            
                        //Partidos jugados como local
                        int pjl = rsP.getInt("pjl"); pjl = pjl +1;
                                                
                        //Partidos empatados 
                        int pe = rsP.getInt("pe"); pe = pe +1;             
                        //Partidos empatados como local
                        int pel = rsP.getInt("pel"); pel = pel +1;                   
                        
                        //Goles
                            //Goles a favor
                        int gf = rsP.getInt("gf"); gf = gf + gL;                        
                            //Goles en contra
                        int gc = rsP.getInt("gc"); gc = gc + gV;                        
                        
                        statement3.executeUpdate("update Posiciones set pts = " + pts +
                                                    ", pj = "+ pj +
                                                    ", pjl = "+ pjl +
                                                    ", pe = "+ pe +
                                                    ", pel = "+ pel +
                                                    ", gf = "+ gf +
                                                    ", gc = "+ gc +                                                               
                                                    " where equipo = '"+ equipoL +"' ;");
                                                    
                      }
                      
                      if (rsP.getString("equipo").equals(equipoV)) {
                        // Puntos  
                        int pts = rsP.getInt("pts"); pts = pts + 1;                        
                                  
                        // Partidos jugados 
                        int pj = rsP.getInt("pj"); pj = pj + 1;                       
                              
                        //Partidos jugados como visitante
                        int pjv = rsP.getInt("pjv"); pjv = pjv + 1;                     
                        
                        // Partidos Empatados como visitante
                        int pe = rsP.getInt("pe"); pe = pe + 1;                       
                           
                        // Partidos Empatados como visitante
                        int pev = rsP.getInt("pev"); pev = pev + 1;                       
                                        
                        //Goles
                            //Goles a favor
                        int gf = rsP.getInt("gf"); gf = gf + gV;                        
                            //Goles en contra
                        int gc = rsP.getInt("gc"); gc = gc + gL;
       
                        
                        statement3.executeUpdate("update Posiciones set pts = " + pts +
                                                    ", pj = "+ pj +
                                                    ", pjv = "+ pjv +
                                                    ", pe = "+ pe+
                                                    ", pev = "+ pev +
                                                    ", gf = "+ gf +
                                                    ", gc = "+ gc +                                                                                 
                                                    " where equipo = '"+ equipoV +"' ;");
                                                     
                      }      
                      
                  }   
                      
                  }else{
                      //System.out.println("Visitante");
                      //-------------------------Codigo si gana Visitante------------------------------
                       
                       while (rsP.next()){
                      
                        if (rsP.getString("equipo").equals(equipoV) ) {
                        // Puntos  
                        int pts = rsP.getInt("pts"); pts = pts + 3;                        
                                                
                        //Partidos jugados
                        int pj = rsP.getInt("pj"); pj = pj +1;
                                                
                        //Partidos jugados
                        int pjv = rsP.getInt("pjv"); pjv = pjv +1;
                                               
                        //Partidos ganados 
                        int pg = rsP.getInt("pg"); pg = pg +1;
                                   
                        //Partidos ganados como visitante
                        int pgv = rsP.getInt("pgv"); pgv = pgv +1;
                                             
                        //Goles
                            //Goles a favor
                        int gf = rsP.getInt("gf"); gf = gf + gV;
                            //Goles en contra
                        int gc = rsP.getInt("gc"); gc = gc + gL;                        
                            //Gol diferencia
                        int gd = rsP.getInt("gd"); gd = gd + (gV - gL);
                                                
                        statement3.executeUpdate("update Posiciones set pts = " + pts +
                                                    ", pj = "+ pj +
                                                    ", pjv = "+ pjv +
                                                    ", pg = "+ pg +
                                                    ", pgv = "+ pgv +
                                                    ", gf = "+ gf +
                                                    ", gc = "+ gc +
                                                    ", gd = "+ gd +                                
                                                    " where equipo = '"+ equipoV +"' ;"); 
                        
                      }
                      
                      if (rsP.getString("equipo").equals(equipoL)) {
                          
                        // Partidos jugados 
                        int pj = rsP.getInt("pj"); pj = pj + 1;                        
                                              
                        //Partidos jugados como local
                        int pjl = rsP.getInt("pjl"); pjl = pjl + 1;                        
                                              
                        // Partidos Perdidos
                        int pp = rsP.getInt("pp"); pp = pp + 1; 
                        // Partidos Perdidos como local
                        int ppl = rsP.getInt("ppl"); ppl = ppl + 1;                        
                                            
                        //Goles
                            //Goles a favor
                        int gf = rsP.getInt("gf"); gf = gf + gL;
                            //Goles en contra
                        int gc = rsP.getInt("gc"); gc = gc + gV;
                            //Gol diferencia
                        int gd = rsP.getInt("gd"); gd = gd + (gL - gV);
                        
                        statement3.executeUpdate("update Posiciones set pj = " + pj +                                                   
                                                    ", pjl = "+ pjl +
                                                    ", pp = "+ pp +
                                                    ", ppl = "+ ppl +
                                                    ", gf = "+ gf +
                                                    ", gc = "+ gc +
                                                    ", gd = "+ gd +                                
                                                    " where equipo = '"+ equipoL +"' ;"); 
                          
                      }           
                      
                    }    
                  }
              
              }
        
        }
        catch(SQLException e){
        
        }finally{
        
        }
        
      
    }


public static double realizarApuesta (String resultado, double cuotaL,
                                        double cuotaE, double cuotaV,
                                        String Bet, double Cuenta, double ROI, double aux){
    
    System.out.println("Resultado: " + resultado + " | Sugerencia Bet: " +Bet );
    //System.out.println(String.valueOf(Cuenta + " Cuenta"));
    
    if (resultado.equals(Bet)) {
               
        switch(Bet){
            case "H": Cuenta = Cuenta + (cuotaL*1) - 1;
                break;
            case "D": Cuenta = Cuenta + (cuotaE*1) -1 ;
                break;
            case "A": Cuenta = Cuenta + (cuotaV*1) -1;
                break;
               
        }                      
        
        
        
    }else{
        if (Bet.equals("Z")) {
            
        }else{
            Cuenta = Cuenta - 1;
        }
    
            
    }
    
    return Cuenta;
    

}
}




