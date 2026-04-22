package com.green.eats.store.application;

import com.green.eats.common.model.MenuGetClientRes;
import com.green.eats.store.application.model.MenuGetRes;
import com.green.eats.store.application.model.MenuPostReq;
import com.green.eats.store.entity.Menu;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class StoreService {
    private final MenuRepository menuRepository;

    public void addMenu(MenuPostReq req) {
        Menu menu = new Menu(req);
        menuRepository.save(menu);
    }

    public List<MenuGetRes> getAllMenus(){
        List<Menu> menuList = menuRepository.findAll();
        List<MenuGetRes> resList = new ArrayList<>( menuList.size() );
         for(Menu item : menuList) {
             MenuGetRes res = new MenuGetRes(item);
             resList.add(res);

         }
         //스트림 방식
         List<MenuGetRes> resList2 = menuList.stream()
                 .map(MenuGetRes::new).toList();
         return resList;
    }

    public Map<Long, MenuGetClientRes> getMenuListByIds(List<Long> menuIds) {
        // 1. Repository에서 IN 절을 사용하여 일괄 조회
        List<Menu> menus = menuRepository.findAllById(menuIds);

        Map<Long, MenuGetClientRes> map = new HashMap<>();
        for(Menu menu : menus) {
            Long key = menu.getId();
            MenuGetClientRes value = MenuGetClientRes.builder()
                    .menuId(menu.getId())
                    .name(menu.getName())
                    .price(menu.getPrice())
                    .build();
            map.put(key, value);
        }
        return map;

//        // 2. List > Map 변환 (Java Stream 사용)
//        return menus.stream()
//                .collect(Collectors.toMap( Menu::getId, menu -> MenuGetClientRes.builder()
//                        .menuId(menu.getId())
//                        .name(menu.getName())
//                        .price(menu.getPrice())
//                        .build()
//                ));
    }
}
