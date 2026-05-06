package icu.iseenu.stock.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import icu.iseenu.stock.entity.Stock;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 股票Mapper接口
 */
@Mapper
public interface StockMapper extends BaseMapper<Stock> {

    /**
     * 根据股票代码查询
     * @param stockCode 股票代码
     * @return 股票实体
     */
    @Select("SELECT * FROM stocks WHERE stock_code = #{stockCode} AND deleted = 0")
    Stock selectByStockCode(@Param("stockCode") String stockCode);

    /**
     * 根据股票类型查询列表
     * @param stockType 股票类型
     * @return 股票列表
     */
    @Select("SELECT * FROM stocks WHERE stock_type = #{stockType} AND deleted = 0")
    List<Stock> selectByStockType(@Param("stockType") String stockType);

    /**
     * 查询持仓数量大于指定值的股票
     * @param minQuantity 最小持仓数量
     * @return 股票列表
     */
    @Select("SELECT * FROM stocks WHERE holding_quantity > #{minQuantity} AND deleted = 0")
    List<Stock> selectStocksWithMinQuantity(@Param("minQuantity") Long minQuantity);

    /**
     * 检查股票代码是否存在
     * @param stockCode 股票代码
     * @return 存在返回true
     */
    @Select("SELECT COUNT(*) > 0 FROM stocks WHERE stock_code = #{stockCode} AND deleted = 0")
    boolean existsByStockCode(@Param("stockCode") String stockCode);

    /**
     * 根据股票代码删除（逻辑删除）
     * @param stockCode 股票代码
     * @return 删除的行数
     */
    int deleteByStockCode(@Param("stockCode") String stockCode);
}
