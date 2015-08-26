/*******************************************************************************
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package emlab.gen.util;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.math.stat.regression.SimpleRegression;

/**
 * @author manebel, jrichstein
 *
 */
public class PredictionAnalysis {

    /**
     * @param args
     */
    public static void main(String[] args) {
        // read in data
        String inputFolder = "/home/jrichstein/emlab-generation/emlab-generation"
                + "/src/main/resources/data/stochasticFuelPrices/";

        String outputFile = "/home/jrichstein/Desktop/emlabGen/PredictionAnalyse/";
        // First year, where Prediction shall be calculated for
        int startYear = 2022;
        // Last year, where Prediction shall be calculated for
        int endYear = 2050;
        double[] confidenceLevels = { 0.95 };

        // Create data for different confidence levels
        for (int i = 0; i < confidenceLevels.length; i++) {
            createData(startYear, endYear, confidenceLevels[i], outputFile, inputFolder);
        }
    }

    public static void createData(int startingYear, int endYear, double predictionInterval, String outputFile,
            String inputFolder) {
        BufferedReader br = null;
        String line = "";
        String cvsSplitBy = ",";
        String[] years = new String[200];
        String[] coalMedium = new String[200];
        String[] gasMedium = new String[200];
        StringBuffer bufCoal = new StringBuffer();
        StringBuffer bufGas = new StringBuffer();
        bufCoal.append("stochId,year,real,prediction-6-8,prediction-4-6,prediction-4-8,prediction-6-6,prediction-5-7\n");
        bufGas.append("stochId,year,real,prediction-6-8,prediction-4-6,prediction-4-8,prediction-6-6,prediction-5-7\n");

        for (int j = 1; j <= 120; j++) {
            String filename = "fuelPrices-" + j + ".csv";
            String fuelFile = inputFolder + filename;
            try {

                br = new BufferedReader(new FileReader(fuelFile));
                int i = 0;
                while ((line = br.readLine()) != null) {
                    if (i == 0) {
                        years = line.split(cvsSplitBy);
                    }
                    if (i == 1) {
                        coalMedium = line.split(cvsSplitBy);
                    }
                    if (i == 2) {
                        gasMedium = line.split(cvsSplitBy);
                    }
                    i++;

                }

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            // FORECASTS FOR COAL PRICES
            // according to different values for numberOfYearsBackLooking and
            // futureTimePoint
            double[][] coal1 = new double[endYear - startingYear + 1][3];
            coal1 = getData(6, 8, predictionInterval, coalMedium, years, startingYear, endYear);
            double[][] coal2 = new double[endYear - startingYear + 1][3];
            coal2 = getData(4, 6, predictionInterval, coalMedium, years, startingYear, endYear);
            double[][] coal3 = new double[endYear - startingYear + 1][3];
            coal3 = getData(4, 8, predictionInterval, coalMedium, years, startingYear, endYear);
            double[][] coal4 = new double[endYear - startingYear + 1][3];
            coal4 = getData(6, 6, predictionInterval, coalMedium, years, startingYear, endYear);
            double[][] coal5 = new double[endYear - startingYear + 1][3];
            coal5 = getData(5, 7, predictionInterval, coalMedium, years, startingYear, endYear);
            // Calculating the average. There are 4 agents, that have
            // numberOfYearsBackLooking: 6 and futureTimePoint: 8
            for (int i = 0; i < endYear - startingYear + 1; i++) {
                bufCoal.append(j);
                bufCoal.append(",");
                bufCoal.append(coal1[i][0]);
                bufCoal.append(",");
                bufCoal.append(coal1[i][2]);
                bufCoal.append(",");
                bufCoal.append(coal1[i][1]);
                bufCoal.append(",");
                bufCoal.append(coal2[i][1]);
                bufCoal.append(",");
                bufCoal.append(coal3[i][1]);
                bufCoal.append(",");
                bufCoal.append(coal4[i][1]);
                bufCoal.append(",");
                bufCoal.append(coal5[i][1]);
                bufCoal.append("\n");

            }

            // FORECASTS FOR GAS PRICES
            // according to different values for numberOfYearsBackLooking and
            // futureTimePoint
            double[][] gas1 = new double[endYear - startingYear + 1][3];
            gas1 = getData(6, 8, predictionInterval, gasMedium, years, startingYear, endYear);
            double[][] gas2 = new double[endYear - startingYear + 1][3];
            gas2 = getData(4, 6, predictionInterval, gasMedium, years, startingYear, endYear);
            double[][] gas3 = new double[endYear - startingYear + 1][3];
            gas3 = getData(4, 8, predictionInterval, gasMedium, years, startingYear, endYear);
            double[][] gas4 = new double[endYear - startingYear + 1][3];
            gas4 = getData(6, 6, predictionInterval, gasMedium, years, startingYear, endYear);
            double[][] gas5 = new double[endYear - startingYear + 1][3];
            gas5 = getData(5, 7, predictionInterval, gasMedium, years, startingYear, endYear);
            // Calculating the average. There are 4 agents, that have
            // numberOfYearsBackLooking: 6 and futureTimePoint: 8
            for (int i = 0; i < endYear - startingYear + 1; i++) {
                bufGas.append(j);
                bufGas.append(",");
                bufGas.append(gas1[i][0]);
                bufGas.append(",");
                bufGas.append(gas1[i][2]);
                bufGas.append(",");
                bufGas.append(gas1[i][1]);
                bufGas.append(",");
                bufGas.append(gas2[i][1]);
                bufGas.append(",");
                bufGas.append(gas3[i][1]);
                bufGas.append(",");
                bufGas.append(gas4[i][1]);
                bufGas.append(",");
                bufGas.append(gas5[i][1]);
                bufGas.append("\n");

            }
        }

        try {
            FileWriter writer = new FileWriter(outputFile + "Coal" + "_" + predictionInterval + ".csv");
            writer.append(bufCoal);
            writer.flush();
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            FileWriter writer = new FileWriter(outputFile + "Gas" + "_" + predictionInterval + ".csv");
            writer.append(bufGas);
            writer.flush();
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static double[][] getData(int yearsBack, int yearsAhead, double alpha, String[] data, String[] years,
            int startYear, int endYear) {
        double[][] result = new double[endYear - startYear + 1][3];
        int currentYear = startYear - yearsAhead;
        int i = 0;
        while (currentYear + yearsAhead <= endYear) {
            int startToCollect = Math.max(1, currentYear - 2011 - yearsBack);
            SimpleRegression sr = new SimpleRegression();
            while (startToCollect <= currentYear - 2011) {
                sr.addData(Integer.parseInt(years[startToCollect]), Double.parseDouble(data[startToCollect]));
                startToCollect++;
            }

            result[i][0] = Integer.parseInt(years[currentYear - 2011 + yearsAhead]);
            result[i][1] = sr.predict(currentYear + yearsAhead);
            // result[i][2] = Math.max(0, sr.getPredictionInterval(currentYear +
            // yearsAhead, alpha)[0]);
            // result[i][3] = sr.getPredictionInterval(currentYear + yearsAhead,
            // alpha)[1];
            result[i][2] = Double.parseDouble(data[currentYear - 2011 + yearsAhead]);

            i++;
            currentYear++;
        }
        return result;
    }
}
