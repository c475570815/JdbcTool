package indi.cyh.sailjdbc;

import indi.cyh.sailjdbc.core.DataSourceFactory;
import indi.cyh.sailjdbc.core.JdbcDataBase;


public class StartApplication {
    static JdbcDataBase db = DataSourceFactory.getJdbcDataBase();

    public static void main(String[] args) throws Exception {


    }
}

