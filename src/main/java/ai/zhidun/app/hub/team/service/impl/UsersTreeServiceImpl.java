package ai.zhidun.app.hub.team.service.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import ai.zhidun.app.hub.team.dao.Organ;
import ai.zhidun.app.hub.team.dao.OrganMapper;
import ai.zhidun.app.hub.team.dao.PersonsInfo;
import ai.zhidun.app.hub.team.dao.UsersTree;
import ai.zhidun.app.hub.team.dao.UsersTreeMapper;
import ai.zhidun.app.hub.team.service.UsersTreeService;

@Service("UsersTreeService")
public class UsersTreeServiceImpl extends ServiceImpl<UsersTreeMapper, PersonsInfo> implements UsersTreeService {
	private final UsersTreeMapper usersTreeMapper;
	
	private final OrganMapper organMapper;
	
	public UsersTreeServiceImpl(UsersTreeMapper usersTreeMapper,OrganMapper organMapper) {
        this.usersTreeMapper = usersTreeMapper;
        this.organMapper = organMapper;
    }
	
	public List<UsersTree> getUsersTree() {
		List<UsersTree> resulTrees = new ArrayList<UsersTree>();
		
		//查询整体组织架构
		LambdaQueryWrapper<Organ> query = Wrappers
                .lambdaQuery(Organ.class)
                .select(Organ::getId,Organ::getOrganFather,Organ::getOrganName,Organ::getStatus)
                .eq(Organ::getStatus,"1");
		
		List<Organ> organ = organMapper.selectList(query);
		Iterator<Organ> iterator = organ.iterator();
		while (iterator.hasNext()) {
			Organ tmp = iterator.next();
			UsersTree resultTmp = new UsersTree();
			if (tmp.getId() != null) {			
				String id = tmp.getId();
				resultTmp.setId(id);
				if (tmp.getOrganName()!=null) {
					resultTmp.setOrganName(tmp.getOrganName());
				}
				if (tmp.getOrganFather()!=null) {
					resultTmp.setOrganFather(tmp.getOrganFather());
				}
				if (tmp.getStatus()!=null) {
					resultTmp.setStatus(tmp.getStatus());
				}
				//获取成员信息
				JSONArray personArray = new JSONArray();
				//查询所有人员信息
				LambdaQueryWrapper<PersonsInfo> queryPerson = Wrappers
		                .lambdaQuery(PersonsInfo.class)
		                .select(PersonsInfo::getId,PersonsInfo::getName,PersonsInfo::getOfficer,PersonsInfo::getIfLeader,PersonsInfo::getOrganId)
		                .eq(PersonsInfo::getOrganId, id);
				List<PersonsInfo> personList = usersTreeMapper.selectList(queryPerson);
				Iterator<PersonsInfo> personIterator = personList.iterator();
				while (personIterator.hasNext()) {
					PersonsInfo tmpPersonsInfo = personIterator.next();
					if (tmpPersonsInfo.getOfficer().equals("局长") || tmpPersonsInfo.getOfficer().equals("副局长") ||tmpPersonsInfo.getOfficer().equals("政委")) {
						tmpPersonsInfo.setOfficer("特殊用户");
					}else {
						tmpPersonsInfo.setOfficer("普通用户");						
					}
					personArray.add(tmpPersonsInfo);
				}
				
				resultTmp.setPersonArray(personArray);
				resulTrees.add(resultTmp);
			}
		}
		
		return resulTrees;
	}

}
