package com.controller;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Date;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

import com.utils.ValidatorUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.annotation.IgnoreAuth;

import com.entity.KeyanchengguoEntity;
import com.entity.view.KeyanchengguoView;

import com.service.KeyanchengguoService;
import com.service.TokenService;
import com.utils.PageUtils;
import com.utils.R;
import com.utils.MD5Util;
import com.utils.MPUtil;
import com.utils.CommonUtil;
import java.io.IOException;

/**
 * 科研成果
 * 后端接口
 * @author 
 * @email 
 * @date 2022-03-20 10:50:00
 */
@RestController
@RequestMapping("/keyanchengguo")
public class KeyanchengguoController {
    @Autowired
    private KeyanchengguoService keyanchengguoService;

    @Autowired
    private com.service.JiaoshiService jiaoshiService;
    


    /**
     * 后端列表
     */
    @RequestMapping("/page")
    public R page(@RequestParam Map<String, Object> params,KeyanchengguoEntity keyanchengguo,
		HttpServletRequest request){
		String tableName = request.getSession().getAttribute("tableName").toString();
		if(tableName.equals("jiaoshi")) {
			keyanchengguo.setJiaoshigonghao((String)request.getSession().getAttribute("username"));
		}
        EntityWrapper<KeyanchengguoEntity> ew = new EntityWrapper<KeyanchengguoEntity>();
		PageUtils page = keyanchengguoService.queryPage(params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, keyanchengguo), params), params));

        return R.ok().put("data", page);
    }
    
    /**
     * 前端列表
     */
	@IgnoreAuth
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params,KeyanchengguoEntity keyanchengguo, 
		HttpServletRequest request){
        EntityWrapper<KeyanchengguoEntity> ew = new EntityWrapper<KeyanchengguoEntity>();
		PageUtils page = keyanchengguoService.queryPage(params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, keyanchengguo), params), params));
        return R.ok().put("data", page);
    }

	/**
     * 列表
     */
    @RequestMapping("/lists")
    public R list( KeyanchengguoEntity keyanchengguo){
       	EntityWrapper<KeyanchengguoEntity> ew = new EntityWrapper<KeyanchengguoEntity>();
      	ew.allEq(MPUtil.allEQMapPre( keyanchengguo, "keyanchengguo")); 
        return R.ok().put("data", keyanchengguoService.selectListView(ew));
    }

	 /**
     * 查询
     */
    @RequestMapping("/query")
    public R query(KeyanchengguoEntity keyanchengguo){
        EntityWrapper< KeyanchengguoEntity> ew = new EntityWrapper< KeyanchengguoEntity>();
 		ew.allEq(MPUtil.allEQMapPre( keyanchengguo, "keyanchengguo")); 
		KeyanchengguoView keyanchengguoView =  keyanchengguoService.selectView(ew);
		return R.ok("查询科研成果成功").put("data", keyanchengguoView);
    }
	
    /**
     * 后端详情
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id){
        KeyanchengguoEntity keyanchengguo = keyanchengguoService.selectById(id);
        return R.ok().put("data", keyanchengguo);
    }

    /**
     * 前端详情
     */
	@IgnoreAuth
    @RequestMapping("/detail/{id}")
    public R detail(@PathVariable("id") Long id){
        KeyanchengguoEntity keyanchengguo = keyanchengguoService.selectById(id);
        return R.ok().put("data", keyanchengguo);
    }
    



    /**
     * 后端保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody KeyanchengguoEntity keyanchengguo, HttpServletRequest request){
    	keyanchengguo.setId(new Date().getTime()+new Double(Math.floor(Math.random()*1000)).longValue());
    	//ValidatorUtils.validateEntity(keyanchengguo);
        // ======== 【区块链双轨确权核心】 ========
        try {
            String uploadedFile = keyanchengguo.getChengguotupian();
            if (org.apache.commons.lang3.StringUtils.isNotBlank(uploadedFile)) {
                String exactFileName = uploadedFile.substring(uploadedFile.lastIndexOf("/") + 1); // 提取出文件名

                String ipfsHash = com.controller.FileController.IPFS_CACHE.get(exactFileName);
                if (ipfsHash != null) {
                    keyanchengguo.setIpfsHash(ipfsHash); // 把 IPFS 存入实体

                    // 获取当前登录老师的真实区块链地址
                    String teacherNo = (String) request.getSession().getAttribute("username");
                    com.entity.JiaoshiEntity teacher = jiaoshiService.selectOne(
                            new com.baomidou.mybatisplus.mapper.EntityWrapper<com.entity.JiaoshiEntity>().eq("jiaoshigonghao", teacherNo)
                    );
                    String teacherAddress = (teacher != null && teacher.getBlockchainAddress() != null)
                            ? teacher.getBlockchainAddress() : "0x0000000000000000000000000000000000000000";

                    // 公证人代付 Gas 上链
                    System.out.println("🚀 正在将科研成果上链...");
                    String txHash = com.utils.BlockChainUtil.submitResearchToChain(
                            String.valueOf(keyanchengguo.getId()),
                            keyanchengguo.getChengguomingcheng(),
                            ipfsHash,
                            teacherAddress
                    );
                    keyanchengguo.setBlockchainTxHash(txHash); // 把以太坊交易哈希存入实体

                    // 扫地僧：用完清理缓存，防止挤爆内存
                    com.controller.FileController.IPFS_CACHE.remove(exactFileName);
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        // ===================================
        keyanchengguoService.insert(keyanchengguo);
        return R.ok();
    }
    
    /**
     * 前端保存
     */
    @RequestMapping("/add")
    public R add(@RequestBody KeyanchengguoEntity keyanchengguo, HttpServletRequest request){
    	keyanchengguo.setId(new Date().getTime()+new Double(Math.floor(Math.random()*1000)).longValue());
    	//ValidatorUtils.validateEntity(keyanchengguo);
        keyanchengguoService.insert(keyanchengguo);
        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody KeyanchengguoEntity keyanchengguo, HttpServletRequest request){
        //ValidatorUtils.validateEntity(keyanchengguo);
        keyanchengguoService.updateById(keyanchengguo);//全部更新
        return R.ok();
    }
    

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids){
        keyanchengguoService.deleteBatchIds(Arrays.asList(ids));
        return R.ok();
    }
    
    /**
     * 提醒接口
     */
	@RequestMapping("/remind/{columnName}/{type}")
	public R remindCount(@PathVariable("columnName") String columnName, HttpServletRequest request, 
						 @PathVariable("type") String type,@RequestParam Map<String, Object> map) {
		map.put("column", columnName);
		map.put("type", type);
		
		if(type.equals("2")) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			Calendar c = Calendar.getInstance();
			Date remindStartDate = null;
			Date remindEndDate = null;
			if(map.get("remindstart")!=null) {
				Integer remindStart = Integer.parseInt(map.get("remindstart").toString());
				c.setTime(new Date()); 
				c.add(Calendar.DAY_OF_MONTH,remindStart);
				remindStartDate = c.getTime();
				map.put("remindstart", sdf.format(remindStartDate));
			}
			if(map.get("remindend")!=null) {
				Integer remindEnd = Integer.parseInt(map.get("remindend").toString());
				c.setTime(new Date());
				c.add(Calendar.DAY_OF_MONTH,remindEnd);
				remindEndDate = c.getTime();
				map.put("remindend", sdf.format(remindEndDate));
			}
		}
		
		Wrapper<KeyanchengguoEntity> wrapper = new EntityWrapper<KeyanchengguoEntity>();
		if(map.get("remindstart")!=null) {
			wrapper.ge(columnName, map.get("remindstart"));
		}
		if(map.get("remindend")!=null) {
			wrapper.le(columnName, map.get("remindend"));
		}

		String tableName = request.getSession().getAttribute("tableName").toString();
		if(tableName.equals("jiaoshi")) {
			wrapper.eq("jiaoshigonghao", (String)request.getSession().getAttribute("username"));
		}

		int count = keyanchengguoService.selectCount(wrapper);
		return R.ok().put("count", count);
	}
	






    /**
     * （按值统计）
     */
    @RequestMapping("/value/{xColumnName}/{yColumnName}")
    public R value(@PathVariable("yColumnName") String yColumnName, @PathVariable("xColumnName") String xColumnName,HttpServletRequest request) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("xColumn", xColumnName);
        params.put("yColumn", yColumnName);
        EntityWrapper<KeyanchengguoEntity> ew = new EntityWrapper<KeyanchengguoEntity>();
		String tableName = request.getSession().getAttribute("tableName").toString();
		if(tableName.equals("jiaoshi")) {
            ew.eq("jiaoshigonghao", (String)request.getSession().getAttribute("username"));
		}
        List<Map<String, Object>> result = keyanchengguoService.selectValue(params, ew);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        for(Map<String, Object> m : result) {
            for(String k : m.keySet()) {
                if(m.get(k) instanceof Date) {
                    m.put(k, sdf.format((Date)m.get(k)));
                }
            }
        }
        return R.ok().put("data", result);
    }

    /**
     * （按值统计）时间统计类型
     */
    @RequestMapping("/value/{xColumnName}/{yColumnName}/{timeStatType}")
    public R valueDay(@PathVariable("yColumnName") String yColumnName, @PathVariable("xColumnName") String xColumnName, @PathVariable("timeStatType") String timeStatType,HttpServletRequest request) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("xColumn", xColumnName);
        params.put("yColumn", yColumnName);
        params.put("timeStatType", timeStatType);
        EntityWrapper<KeyanchengguoEntity> ew = new EntityWrapper<KeyanchengguoEntity>();
        String tableName = request.getSession().getAttribute("tableName").toString();
        if(tableName.equals("jiaoshi")) {
            ew.eq("jiaoshigonghao", (String)request.getSession().getAttribute("username"));
        }
        List<Map<String, Object>> result = keyanchengguoService.selectTimeStatValue(params, ew);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        for(Map<String, Object> m : result) {
            for(String k : m.keySet()) {
                if(m.get(k) instanceof Date) {
                    m.put(k, sdf.format((Date)m.get(k)));
                }
            }
        }
        return R.ok().put("data", result);
    }

    /**
     * 分组统计
     */
    @RequestMapping("/group/{columnName}")
    public R group(@PathVariable("columnName") String columnName,HttpServletRequest request) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("column", columnName);
        EntityWrapper<KeyanchengguoEntity> ew = new EntityWrapper<KeyanchengguoEntity>();
        String tableName = request.getSession().getAttribute("tableName").toString();
        if(tableName.equals("jiaoshi")) {
            ew.eq("jiaoshigonghao", (String)request.getSession().getAttribute("username"));
        }
        List<Map<String, Object>> result = keyanchengguoService.selectGroup(params, ew);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        for(Map<String, Object> m : result) {
            for(String k : m.keySet()) {
                if(m.get(k) instanceof Date) {
                    m.put(k, sdf.format((Date)m.get(k)));
                }
            }
        }
        return R.ok().put("data", result);
    }
    /**
     * 【区块链防伪溯源验证接口】
     */
    @IgnoreAuth
    @RequestMapping("/verify/{id}")
    public R verify(@PathVariable("id") Long id) {
        KeyanchengguoEntity research = keyanchengguoService.selectById(id);
        if (research == null) {
            return R.error("未找到该科研成果");
        }
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("成果名称", research.getChengguomingcheng());
        result.put("IPFS全网分布式指纹", research.getIpfsHash() != null ? research.getIpfsHash() : "未上链");

        if (research.getBlockchainTxHash() != null && !research.getBlockchainTxHash().startsWith("CHAIN_FAILED")) {
            result.put("区块链确权状态", "✅ 数据已通过区块链不可篡改验证");
            result.put("底层交易TxHash", research.getBlockchainTxHash());
            result.put("确权说明", "此为去中心化验证，数据永久保存在Ganache节点。");
        } else {
            result.put("区块链确权状态", "⚠️ 暂未上链或数据异常");
        }
        return R.ok().put("data", result);
    }

}
