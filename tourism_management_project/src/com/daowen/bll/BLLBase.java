package com.daowen.bll;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import net.sf.json.util.JavaIdentifierTransformer;
import net.sf.json.util.PropertySetStrategy;




import com.daowen.dal.DALBase;
import com.daowen.util.DateJsonValueProcessor;
import com.daowen.util.NameUtil;
import com.daowen.util.TotalRow;
import com.sun.org.apache.commons.beanutils.PropertyUtils;

public class BLLBase<T> {

	 
	
	public  String   getGridJson(String tablename,String filter){
		
		List list=DALBase.getEntity(tablename, filter);
		
		
		JsonConfig config = new JsonConfig(); 
		
	  /*  config.setPropertySetStrategy(new PropertySetStrategy(){
	        @Override
	        public void setProperty(Object bean, String key, Object value)throws JSONException {
		            try {
	   
	                PropertyUtils.setNestedProperty(bean, NameUtil.toFirstUpper(key), value);
	 
	            } catch (Exception e) {
	   
	                e.printStackTrace();
	    
	            }
	   
	        }
	    });*/

		//
		config.registerJsonValueProcessor(Timestamp.class,new DateJsonValueProcessor("yyyy-MM-dd HH:mm:ss"));
		config.setJavaIdentifierTransformer(new JavaIdentifierTransformer() {


		      @Override
		      public String transformToJavaIdentifier(String str) {
		        char[] chars = str.toCharArray();
		        chars[0] = Character.toLowerCase(chars[0]);
		        return new String(chars);
		      }
		      
		    });
		config.registerJsonValueProcessor(Date.class,new DateJsonValueProcessor("yyyy-MM-dd HH:mm:ss"));  
        TotalRow tr=new TotalRow();
        tr.setRows(list);
        tr.setTotal(list.size());
        
		JSONObject jsonobject = JSONObject.fromObject(tr,config);
		//config.registerJsonValueProcessor(Date.class,new DateJsonValueProcessor("yyyy-MM-dd HH:mm:ss"));
		//JSONObject jsonobject=JSONObject.fromObject(new Object(),config).element("Rows", list);
		//jsonobject.element("Total", list.size());
		
	  	return jsonobject.toString();
	}
	
	
	 public  String getComboJson(String tablename,String filter,String id,String text)
	  {
		  List<T> list=DALBase.getEntity(tablename,filter);
		  StringBuffer sb=new StringBuffer();
		
		  sb.append("[");
		  for(Iterator<T> it=list.iterator();it.hasNext();){
			  
			  T t=it.next();
			  try {
				
				  String temtext="";
				  String temid="";
				try {
					temtext = t.getClass().getMethod("get"+NameUtil.toFirstUpper(text), null).invoke(t, null).toString();
				    temid=t.getClass().getMethod("get"+NameUtil.toFirstUpper(id), null).invoke(t, null).toString();
					
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			
				sb.append("{\"id\":\""+temid+"\",\"text\":\""+temtext+"\"}");
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			  if(it.hasNext())
				  sb.append(",");
			  
		  }
		  sb.append("]");
		  System.out.println("JSON="+sb.toString());
		  return sb.toString();
	  }
	 /***
	  * @param tablename
	  * @param filter
	  * @param parentid
	  * @return
	  */
	 public  String getTreeJson(String tablename,String filter,String  parentidfieldname,String textfieldname,String idfieldname){
		 
		 StringBuffer sb=new StringBuffer();
		 sb.append("[");
		 List<T> list=DALBase.getEntity(tablename," where "+parentidfieldname+"='-1'");
		 
		 
			 sb.append("{id:'-1',isleaf:true,text:'资讯类型'" );
			 if(!list.isEmpty()){
				 sb.append("  ,children:[");
			 }
		 for(Iterator<T> i=list.iterator();i.hasNext();){
			 
			    T t=i.next();
				
				boolean isleaf=true;
				String checkstr="";
				
				String temtext="";
				String temid="";
				
			 	try {
					temtext = t.getClass().getMethod("get"+NameUtil.toFirstUpper(textfieldname), null).invoke(t, null).toString();
					 temid=t.getClass().getMethod("get"+NameUtil.toFirstUpper(idfieldname), null).invoke(t, null).toString();
									
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SecurityException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (NoSuchMethodException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			   
				String childs=getChildsJosn(tablename  ,temid,parentidfieldname,textfieldname,idfieldname);
				sb.append("{id:'"+temid+"',isleaf:"+isleaf+checkstr+",text:'"+temtext+"'"+childs+"}" );
				if(i.hasNext())
				   sb.append(",");
			 
		 }
		 //end children
		 if(!list.isEmpty())
		     sb.append("]");
		 sb.append("}]");
		 System.out.print("��JSON="+sb.toString());
		 return sb.toString();
	 }
	 private String getChildsJosn(String tablename,String id, String parentidfieldname,
			String textfieldname, String idfieldname) {
		
		 StringBuffer sb=new StringBuffer();
		 sb.append(" ,children:[");
		 List<T> list=DALBase.getEntity(tablename," where "+parentidfieldname+"='"+id+"'");
		 if(list.isEmpty())
			 return " ";
		 for(Iterator<T> it=list.iterator();it.hasNext();){
			 
			    T t=it.next();
			    boolean isleaf=true;
			   
			    String temtext="";
				String temid="";
				
			 	try {
					
			 		temtext = t.getClass().getMethod("get"+NameUtil.toFirstUpper(textfieldname), null).invoke(t, null).toString();
					temid=t.getClass().getMethod("get"+NameUtil.toFirstUpper(idfieldname), null).invoke(t, null).toString();
									
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SecurityException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (NoSuchMethodException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			    
			    sb.append("{id:'"+temid+"',isleaf:"+isleaf+",text:'"+temtext+"'" );
			    //��Ҷ�ӽڵ�
			    if(isLeaf(tablename, temid, parentidfieldname)){
			    	sb.append(getChildsJosn(tablename, temid, parentidfieldname, textfieldname, idfieldname));
			    	isleaf=false;
			    }
			    //����Ҷ�ӽڵ�
			    sb.append("}");
			    if(it.hasNext())
					   sb.append(",");
		 }
		 sb.append("]");
		 return sb.toString();
	  
		
		
	}
	 
	 private boolean isLeaf(String tablename,String id,String parentidfieldname){
		 
		 List<T> list=DALBase.getEntity(tablename," where "+parentidfieldname+"='"+id+"'");
		 
		 if(list.isEmpty())
			 return true;
		 else
			 
			 return false;
		 
	 }
	 
	 public boolean deleteTreeNode(Class entity,Object id,String parentidfieldname){
		  
	    
		
		T t=(T) DALBase.load(entity, id);
		
		if(t==null)
			return false;
		String tablename=entity.getSimpleName();
		 boolean res=true;
		 try
		 {
		  if(isLeaf(tablename,id.toString(),parentidfieldname))
		      DALBase.delete(t);
		  else
		  {   
			  //ɾ����
		      DALBase.delete(tablename, " where "+parentidfieldname+"='"+id.toString()+"'");
		      //ɾ������
		      
		      DALBase.delete(t);
		  }
		 }catch(Exception e){
			 e.printStackTrace();
			 res=false;
		 }
		 return res;
	 }
	
	

	 
}
