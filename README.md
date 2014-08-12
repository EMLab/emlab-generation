EMLab-Generation - The Market (In-)Stability Reserve for EU Carbon Emission Trading: Why it may fail and how to improve it

This branch of the EMLab-Generation model, contains the version, as well as the complete input data used for the paper The Market (In-)Stability Reserve for EU Carbon Emission Trading: Why it may fail and how to improve it.

With the R-script co2MarketStabilityReserve-scenarioFileCreator.R the necessary scenario files for reproducing the model results of the paper can be created. For manual inspection of the input data, have a look at the following files and locations:

* co2MarketStabilityReserve-scenarioTemplate.xml and co2MarketStabilityReserve-targetCorridorSensitivity-scenarioTemplate.xml are the templates used for generating the scenario run files.
* More complex input data is contained in the following folder: emlab-generation/src/main/resources/data
* The folder emlab-generation/src/main/resources/scenarios/ contains the scenario files SimpleMSR-MSR.xml and SimpleMSR-PureETS.xml used in simplified model in the section on CO2 price volatility.

In order to understand the model two starting points are given here: The file emlab-generation/src/main/java/emlab/gen/role/DecarbonizationModelRole.java contains the overview of modelling steps, and emlab-generation/src/main/java/emlab/gen/role/market/ClearIterativeCO2AndElectricitySpotMarketTwoCountryRole.java contains the electricity and CO2 market clearing algorithm.

This research was developed at *TU Delft, Faculty of Technology Policy and Management, Energy & Industry Section*


Supported by:
* [Energy Delta Gas Research program, project A1 – Understanding gas sector intra-market and inter-market interactions](http://www.edgar-program.com/nl/projects/A1)
* [Knowledge for Climate program, project INCAH – Infrastructure Climate Adaptation in Hotspots] (http://knowledgeforclimate.climateresearchnetherlands.nl/infrastructurenetworks)
* [Erasmus Mundus Joint Doctorate in Sustainable Energy Technologies and Strategies Program](http://www.upcomillas.es/estudios/estu_doct_SETS.aspx)


Please contact Jörn C. Richstein (j.c.richstein@tudelft.nl), Emile Chappin (e.j.l.chappin@tudelft.nl) or Laurens de Vries (L.J.deVries@tudelft.nl) for further information on the paper. For more information on the EMLab-Generation model including instructions on running and installing it, please visit: https://github.com/EMLab/emlab-generation/ or http://emlab.tudelft.nl/ .
