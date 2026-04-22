package com.hivenote.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hivenote.entity.Favorite;
import com.hivenote.mapper.FavoriteMapper;
import com.hivenote.service.IFavoriteService;
import org.springframework.stereotype.Service;

@Service
public class FavoriteServiceImpl extends ServiceImpl<FavoriteMapper, Favorite> implements IFavoriteService {
}
