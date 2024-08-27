/*
 * Copyright (C) 2019 Ruslan Lopez Carro.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
 
// from https://github.com/javatlacati/jcalendar/blob/master/jcalendar/src/main/java/com/toedter/calendar/JHourMinuteChooser.java

package util.com.toedter.calendar;

import java.text.DecimalFormat;
import java.util.Date;
import java.util.logging.Logger;

/**
 *
 * @author Ruslan López Carro
 */
public final class JHourMinuteChooser extends javax.swing.JPanel {

    private Date currentTime;
    private static final Logger LOGGER = Logger.getLogger(JHourMinuteChooser.class.getName());
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JSpinner hourSpin;
    private javax.swing.JSpinner meridianSpin;
    private javax.swing.JSpinner minuteSpin;
    // End of variables declaration//GEN-END:variables


    /**
     * Creates a Hour and Minute Chooser with the specified values.
     *
     * @param hour 24 hour format hour
     * @param minute minutes
     */
    public JHourMinuteChooser(int hour, int minute) {
        setName("JHourMinuteChooser");
        initComponents();
        currentTime = new Date();
        currentTime.setHours(hour);
        currentTime.setMinutes(minute);
        updateCurrentTimeInGUI();
    }

    /**
     * Creates a Hour and Minute Chooser with the current time set.
     */
    public JHourMinuteChooser() {
        setName("JHourMinuteChooser");
        initComponents();
        setCurrentTime();
    }

    @Override
    public void setEnabled(boolean enable) {
        hourSpin.setEnabled(enable);
        minuteSpin.setEnabled(enable);
        meridianSpin.setEnabled(enable);
    }

    public void setCurrentTime() {
        currentTime = new Date();
        updateCurrentTimeInGUI();
    }

    public void setTimeFromString(String toBeParsed){
        if(toBeParsed.matches("\\d{1,2}\\s*\\:\\s*\\d{2}\\s+[AP]M")){
            String[] splitted = toBeParsed.split("\\:");
            currentTime = new Date();
            int hour=Integer.parseInt(splitted[0]);
            if("PM".equals(splitted[2])){
                hour+=12;
            }
            currentTime.setHours(hour);
            int minutes=Integer.parseInt(splitted[1]);
            currentTime.setMinutes(minutes);
        }
        updateCurrentTimeInGUI();
    }

    private void updateCurrentTimeInGUI() {
        LOGGER.finest(currentTime.toString());
        if (currentTime.getHours() >= 0 && currentTime.getHours() < 12) {
            if (currentTime.getHours() == 0) {
                hourSpin.setValue(12);
            } else {
                hourSpin.setValue(currentTime.getHours());
            }
            meridianSpin.setValue("AM");
        } else if (currentTime.getHours() >= 12 && currentTime.getHours() <= 23) {
            if (currentTime.getHours() == 12) {
                hourSpin.setValue(12);
            } else {
                hourSpin.setValue(currentTime.getHours() - 12);
            }
            meridianSpin.setValue("PM");
        }

        // System.out.println("minutes"+currentTime.getMinutes());
        minuteSpin.setValue(numberFormat(currentTime.getMinutes(), "##"));
    }

    public static String numberFormat(long src, String fmt) {//Format : ###.####
        DecimalFormat df = new DecimalFormat(fmt.replaceAll("#", "0"));
        return df.format(src);
    }

    public String getHour() {
        return numberFormat(Integer.parseInt(hourSpin.getValue().toString().trim()), "##");
    }

    public String getMinute() {
        return numberFormat(Integer.parseInt(minuteSpin.getValue().toString().trim()), "##");
    }

    public String getMeridian() {
        return meridianSpin.getValue().toString();
    }

    public String getTime() {
        return numberFormat(Integer.parseInt(hourSpin.getValue().toString().trim()), "##") + ":" + numberFormat(Integer.parseInt(minuteSpin.getValue().toString().trim()), "##") + " " + meridianSpin.getValue().toString();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        hourSpin = new javax.swing.JSpinner();
        minuteSpin = new javax.swing.JSpinner();
        meridianSpin = new javax.swing.JSpinner();

        setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 3, 0));

        hourSpin.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        hourSpin.setModel(new javax.swing.SpinnerNumberModel(1, 1, 12, 1));
        add(hourSpin);

        minuteSpin.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        minuteSpin.setModel(new javax.swing.SpinnerListModel(new String[] {"00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59"}));
        add(minuteSpin);

        meridianSpin.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        meridianSpin.setModel(new javax.swing.SpinnerListModel(new String[] {"AM", "PM"}));
        add(meridianSpin);
    }// </editor-fold>//GEN-END:initComponents


    public Date getCurrentTime() {
        return new Date(currentTime.getTime());
    }
}