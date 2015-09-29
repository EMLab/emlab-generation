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
package emlab.gen.repository;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.annotation.QueryType;
import org.springframework.data.neo4j.repository.GraphRepository;
import org.springframework.data.repository.query.Param;

import emlab.gen.domain.agent.EnergyProducer;
import emlab.gen.domain.market.electricity.FinancialPowerPlantReport;
import emlab.gen.domain.technology.PowerGeneratingTechnology;

/**
 * @author jrichstein
 *
 */
public interface FinancialPowerPlantReportRepository extends GraphRepository<FinancialPowerPlantReport> {

    @Query(value = "techName=g.v(tech).name;"
            + "fr = g.v(producer).in('POWERPLANT_OWNER').as('x').out('TECHNOLOGY').propertyFilter('name',FilterPipe.Filter.EQUAL, techName).back('x').in('FINANCIALREPORT_POWERPLANT').propertyFilter('time', FilterPipe.Filter.GREATER_THAN_EQUAL, from).propertyFilter('time', FilterPipe.Filter.LESS_THAN_EQUAL, to).propertyFilter('powerPlantStatus', FilterPipe.Filter.EQUAL, 1);", type = QueryType.Gremlin)
    public Iterable<FinancialPowerPlantReport> findAllFinancialPowerPlantReportsOfOperationaPlantsFromToForEnergyProducerAndTechnology(
            @Param("from") long from, @Param("to") long to, @Param("producer") EnergyProducer producer,
            @Param("tech") PowerGeneratingTechnology tech);

    @Query(value = "techName=g.v(tech).name; fr = g.v(producer).in('POWERPLANT_OWNER').as('x').out('TECHNOLOGY').propertyFilter('name',FilterPipe.Filter.EQUAL, techName).back('x').in('FINANCIALREPORT_POWERPLANT').propertyFilter('time', FilterPipe.Filter.GREATER_THAN_EQUAL, from).propertyFilter('time', FilterPipe.Filter.LESS_THAN_EQUAL, to).propertyFilter('powerPlantStatus', FilterPipe.Filter.EQUAL, 1);"
            + "if(!fr.hasNext()){return null} else{fr=fr.sort{it.overallRevenue-it.variableCosts}._().toList()};"
            + "length=fr.size(); ninetyfiveQuantile=(int)length*(1-alpha);cvar=(long) 0;"
            + "for(int i=ninetyfiveQuantile; i<=(length-1); i++){cvar=cvar+(fr[i].overallRevenue-fr[i].variableCosts)/fr[i].out('FINANCIALREPORT_POWERPLANT').actualNominalCapacity.next()};"
            + "cvar=cvar/((double)(length - ninetyfiveQuantile+1));" + "return cvar;", type = QueryType.Gremlin)
    public Double calculateHigherBoundaryConventionalTech(@Param("from") long from, @Param("to") long to,
            @Param("producer") EnergyProducer producer, @Param("tech") PowerGeneratingTechnology tech,
            @Param("alpha") double alpha);

    @Query(value = "techName=g.v(tech).name; fr = g.v(producer).in('POWERPLANT_OWNER').as('x').out('TECHNOLOGY').propertyFilter('name',FilterPipe.Filter.EQUAL, techName).back('x').in('FINANCIALREPORT_POWERPLANT').propertyFilter('time', FilterPipe.Filter.GREATER_THAN_EQUAL, from).propertyFilter('time', FilterPipe.Filter.LESS_THAN_EQUAL, to).propertyFilter('powerPlantStatus', FilterPipe.Filter.EQUAL, 1);"
            + "if(!fr.hasNext()){return null} else{fr=fr.sort{it.overallRevenue-it.variableCosts}._().toList()};"
            + "length=fr.size(); fiveQuantile=(int)length*alpha;cvar=(long) 0;"
            + "for(int i=0; i<=fiveQuantile; i++){cvar=cvar+(fr[i].overallRevenue-fr[i].variableCosts)/fr[i].out('FINANCIALREPORT_POWERPLANT').actualNominalCapacity.next()};"
            + "cvar=cvar/((double)(fiveQuantile+1));" + "return cvar;", type = QueryType.Gremlin)
    public Double calculateLowerBoundaryConventionalTech(@Param("from") long from, @Param("to") long to,
            @Param("producer") EnergyProducer producer, @Param("tech") PowerGeneratingTechnology tech,
            @Param("alpha") double alpha);

    @Query("START fr=node:__types__(\"className:emlab.gen.domain.market.electricity.FinancialPowerPlantReport\") WHERE (fr.time={time}) RETURN fr")
    public Iterable<FinancialPowerPlantReport> findAllFinancialPowerPlantReportsForTime(@Param("time") long time);

}
