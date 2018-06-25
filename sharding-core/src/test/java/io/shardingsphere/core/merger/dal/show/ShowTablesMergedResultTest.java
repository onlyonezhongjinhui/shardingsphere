/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.core.merger.dal.show;

import com.google.common.collect.Lists;
import io.shardingsphere.core.api.algorithm.fixture.TestComplexKeysShardingAlgorithm;
import io.shardingsphere.core.api.config.ShardingRuleConfiguration;
import io.shardingsphere.core.api.config.TableRuleConfiguration;
import io.shardingsphere.core.api.config.strategy.ComplexShardingStrategyConfiguration;
import io.shardingsphere.core.merger.QueryResult;
import io.shardingsphere.core.merger.fixture.TestQueryResult;
import io.shardingsphere.core.metadata.ShardingMetaData;
import io.shardingsphere.core.metadata.TableMetaData;
import io.shardingsphere.core.rule.ShardingRule;
import org.junit.Before;
import org.junit.Test;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ShowTablesMergedResultTest {
    
    private ShardingRule shardingRule;
    
    private List<QueryResult> queryResults;
    
    private ResultSet resultSet;
    
    private ShardingMetaData shardingMetaData;
    
    @Before
    public void setUp() throws SQLException {
        TableRuleConfiguration tableRuleConfig = new TableRuleConfiguration();
        tableRuleConfig.setLogicTable("table");
        tableRuleConfig.setActualDataNodes("ds.table_${0..2}");
        tableRuleConfig.setTableShardingStrategyConfig(new ComplexShardingStrategyConfiguration("field1, field2, field3", new TestComplexKeysShardingAlgorithm()));
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.getTableRuleConfigs().add(tableRuleConfig);
        shardingRule = new ShardingRule(shardingRuleConfig, Lists.newArrayList("ds"));
        shardingMetaData = mock(ShardingMetaData.class);
        Map<String, TableMetaData> tableMetaDataMap = new HashMap<>();
        tableMetaDataMap.put("table", new TableMetaData());
        when(shardingMetaData.getTableMetaDataMap()).thenReturn(tableMetaDataMap);
//        when(shardingMetaData.getTableMetaDataMap().keySet()).thenReturn(new HashSet<String>(){{add("table");}});
        
        resultSet = mock(ResultSet.class);
        ResultSetMetaData resultSetMetaData = mock(ResultSetMetaData.class);
        when(resultSet.getMetaData()).thenReturn(resultSetMetaData);
        when(resultSetMetaData.getColumnCount()).thenReturn(1);
        List<ResultSet> resultSets = Lists.newArrayList(resultSet);
        for (ResultSet each : resultSets) {
            when(each.next()).thenReturn(true, false);
        }
        queryResults = new ArrayList<>(resultSets.size());
        for (ResultSet each : resultSets) {
            queryResults.add(new TestQueryResult(each));
        }
    }
    
    @Test
    public void assertNextForEmptyQueryResult() throws SQLException {
        ShowTablesMergedResult showTablesMergedResult = new ShowTablesMergedResult(shardingRule, new ArrayList<QueryResult>(), shardingMetaData);
        assertFalse(showTablesMergedResult.next());
    }
    
    @Test
    public void assertNextForActualTableNameInTableRule() throws SQLException {
        when(resultSet.getObject(1)).thenReturn("table_0");
        ShowTablesMergedResult showTablesMergedResult = new ShowTablesMergedResult(shardingRule, queryResults, shardingMetaData);
        assertTrue(showTablesMergedResult.next());
    }
    
    @Test
    public void assertNextForActualTableNameNotInTableRule() throws SQLException {
        when(resultSet.getObject(1)).thenReturn("table_3");
        System.out.println(shardingMetaData.getTableMetaDataMap());
        ShowTablesMergedResult showTablesMergedResult = new ShowTablesMergedResult(shardingRule, queryResults, shardingMetaData);
        assertFalse(showTablesMergedResult.next());
    }
}
