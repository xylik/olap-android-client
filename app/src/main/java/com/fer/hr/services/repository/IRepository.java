package com.fer.hr.services.repository;

import com.fer.hr.model.CubeMeta;
import com.fer.hr.services.common.Callback;
import com.fer.hr.services.common.IService;

import java.util.List;

/**
 * Created by igor on 17/01/16.
 */
public interface IRepository extends IService {

    void getAllCubesMeta(boolean refreshData, final Callback<List<CubeMeta>> callback);

}
