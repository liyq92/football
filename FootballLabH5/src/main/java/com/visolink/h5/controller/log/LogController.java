package com.visolink.h5.controller.log;

import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.visolink.controller.base.BaseController;
import com.visolink.entity.Page;
import com.visolink.entity.system.Menu;
import com.visolink.h5.service.log.LogService;
import com.visolink.util.AppUtil;
import com.visolink.util.Const;
import com.visolink.util.DateUtil;
import com.visolink.util.ObjectExcelView;
import com.visolink.util.PageData;
import com.visolink.util.Tools;

/** 
 * 类名称：LogController
 * 创建人：FH 
 * 创建时间：2015-05-16
 */
@Controller
@RequestMapping(value="/log")
public class LogController extends BaseController {
	
	@Resource(name="logService")
	private LogService logService;
	
	/**
	 * 新增
	 */
	@RequestMapping(value="/save")
	public ModelAndView save() throws Exception{
		logBefore(logger, "新增Log");
		ModelAndView mv = this.getModelAndView();
		PageData pd = new PageData();
		pd = this.getPageData();
		pd.put("LOG_MEMBER_ID", "");	//会员ID
		pd.put("LOG_VISITOR", "");	//游客信息
		pd.put("LOG_MENU_ID", "");	//栏目ID
		pd.put("LOG_URI_ID", "");	//菜单ID
		pd.put("LOG_OBJECT_ID", "");	//对象ID
		pd.put("LOG_STARTTIME", DateUtil.getTime());	//开始时间
		pd.put("LOG_ENDTIME", DateUtil.getTime());	//结束时间
		pd.put("LOG_LONGITUDE", "");	//经度
		pd.put("LOG_LATITUDE ", "");	//纬度
		pd.put("LOG_KEY_WORD", "");	//关键字
		pd.put("LOG_ERROR", "");	//异常信息
		pd.put("LOG_DATATYPE", "");	//操作类型
		pd.put("LOG_DESCRIPTION", "");	//描述
		pd.put("LOG_SOURCE", "");	//来源
		pd.put("LOG_DEVICE", "");	//来源
		logService.save(pd);
		mv.addObject("msg","success");
		mv.setViewName("save_result");
		return mv;
	}
	
	/**
	 * 删除
	 */
	@RequestMapping(value="/delete")
	public void delete(PrintWriter out){
		logBefore(logger, "删除Log");
		PageData pd = new PageData();
		try{
			pd = this.getPageData();
			logService.delete(pd);
			out.write("success");
			out.close();
		} catch(Exception e){
			logger.error(e.toString(), e);
		}
		
	}
	
	/**
	 * 修改
	 */
	@RequestMapping(value="/edit")
	public ModelAndView edit() throws Exception{
		logBefore(logger, "修改Log");
		ModelAndView mv = this.getModelAndView();
		PageData pd = new PageData();
		pd = this.getPageData();
		logService.edit(pd);
		mv.addObject("msg","success");
		mv.setViewName("save_result");
		return mv;
	}
	
	/**
	 * 列表
	 */
	@RequestMapping(value="/pc/list")
	public ModelAndView list(Page page){
		logBefore(logger, "列表Log");
		ModelAndView mv = this.getModelAndView();
		PageData pd = new PageData();
		try{
			pd = this.getPageData();
			page.setPd(pd);
			List<PageData>	varList = logService.list(page);	//列出Log列表
			mv.setViewName("pc/log/log_list");
			mv.addObject("varList", varList);
			mv.addObject("pd", pd);
			mv.addObject(Const.SESSION_QX,this.getHC());	//按钮权限
		} catch(Exception e){
			logger.error(e.toString(), e);
		}
		return mv;
	}
	
	/**
	 * 去新增页面
	 */
	@RequestMapping(value="/goAdd")
	public ModelAndView goAdd(){
		logBefore(logger, "去新增Log页面");
		ModelAndView mv = this.getModelAndView();
		PageData pd = new PageData();
		pd = this.getPageData();
		try {
			mv.setViewName("log/log_edit");
			mv.addObject("msg", "save");
			mv.addObject("pd", pd);
		} catch (Exception e) {
			logger.error(e.toString(), e);
		}						
		return mv;
	}	
	
	/**
	 * 去修改页面
	 */
	@RequestMapping(value="/goEdit")
	public ModelAndView goEdit(){
		logBefore(logger, "去修改Log页面");
		ModelAndView mv = this.getModelAndView();
		PageData pd = new PageData();
		pd = this.getPageData();
		try {
			pd = logService.findById(pd);	//根据ID读取
			mv.setViewName("log/log_edit");
			mv.addObject("msg", "edit");
			mv.addObject("pd", pd);
		} catch (Exception e) {
			logger.error(e.toString(), e);
		}						
		return mv;
	}	
	
	/**
	 * 批量删除
	 */
	@RequestMapping(value="/pc/deleteAll")
	@ResponseBody
	public Object deleteAll() {
		logBefore(logger, "批量删除Log");
		PageData pd = new PageData();		
		Map<String,Object> map = new HashMap<String,Object>();
		try {
			pd = this.getPageData();
			List<PageData> pdList = new ArrayList<PageData>();
			String DATA_IDS = pd.getString("DATA_IDS");
			if(null != DATA_IDS && !"".equals(DATA_IDS)){
				String ArrayDATA_IDS[] = DATA_IDS.split(",");
				logService.deleteAll(ArrayDATA_IDS);
				pd.put("msg", "ok");
			}else{
				pd.put("msg", "no");
			}
			pdList.add(pd);
			map.put("list", pdList);
		} catch (Exception e) {
			logger.error(e.toString(), e);
		} finally {
			logAfter(logger);
		}
		return AppUtil.returnObject(pd, map);
	}
	
	/*
	 * 导出到excel
	 * @return
	 */
	@RequestMapping(value="/excel")
	public ModelAndView exportExcel(){
		logBefore(logger, "导出Log到excel");
		ModelAndView mv = new ModelAndView();
		PageData pd = new PageData();
		pd = this.getPageData();
		try{
			Map<String,Object> dataMap = new HashMap<String,Object>();
			List<String> titles = new ArrayList<String>();
			titles.add("会员ID");	//1
			titles.add("游客信息");	//2
			titles.add("栏目ID");	//3
			titles.add("菜单ID");	//4
			titles.add("对象ID");	//5
			titles.add("开始时间");	//6
			titles.add("结束时间");	//7
			titles.add("经度");	//8
			titles.add("纬度");	//9
			titles.add("关键字");	//10
			titles.add("异常信息");	//11
			titles.add("操作类型");	//12
			titles.add("描述");	//13
			titles.add("来源");	//14
			dataMap.put("titles", titles);
			List<PageData> varOList = logService.listAll(pd);
			List<PageData> varList = new ArrayList<PageData>();
			for(int i=0;i<varOList.size();i++){
				PageData vpd = new PageData();
				vpd.put("var1", varOList.get(i).get("LOG_MEMBER_ID").toString());	//1
				vpd.put("var2", varOList.get(i).getString("LOG_VISITOR"));	//2
				vpd.put("var3", varOList.get(i).get("LOG_MENU_ID").toString());	//3
				vpd.put("var4", varOList.get(i).get("LOG_URI_ID").toString());	//4
				vpd.put("var5", varOList.get(i).get("LOG_OBJECT_ID").toString());	//5
				vpd.put("var6", varOList.get(i).getString("LOG_STARTTIME"));	//6
				vpd.put("var7", varOList.get(i).getString(" LOG_ENDTIME"));	//7
				vpd.put("var8", varOList.get(i).getString("LOG_LONGITUDE"));	//8
				vpd.put("var9", varOList.get(i).getString("LOG_LATITUDE "));	//9
				vpd.put("var10", varOList.get(i).getString(" LOG_KEY_WORD"));	//10
				vpd.put("var11", varOList.get(i).getString(" LOG_ERROR"));	//11
				vpd.put("var12", varOList.get(i).getString("LOG_DATATYPE"));	//12
				vpd.put("var13", varOList.get(i).getString("LOG_DESCRIPTION"));	//13
				vpd.put("var14", varOList.get(i).getString("LOG_SOURCE"));	//14
				varList.add(vpd);
			}
			dataMap.put("varList", varList);
			ObjectExcelView erv = new ObjectExcelView();
			mv = new ModelAndView(erv,dataMap);
		} catch(Exception e){
			logger.error(e.toString(), e);
		}
		return mv;
	}
	
	/* ===============================权限================================== */
	public Map<String, String> getHC(){
		Subject currentUser = SecurityUtils.getSubject();  //shiro管理的session
		Session session = currentUser.getSession();
		return (Map<String, String>)session.getAttribute(Const.SESSION_QX);
	}
	/* ===============================权限================================== */
	
	@InitBinder
	public void initBinder(WebDataBinder binder){
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		binder.registerCustomEditor(Date.class, new CustomDateEditor(format,true));
	}
}
