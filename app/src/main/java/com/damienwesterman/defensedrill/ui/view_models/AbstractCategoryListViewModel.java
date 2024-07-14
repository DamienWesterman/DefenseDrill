/****************************\
 *      ________________      *
 *     /  _             \     *
 *     \   \ |\   _  \  /     *
 *      \  / | \ / \  \/      *
 *      /  \ | / | /  /\      *
 *     /  _/ |/  \__ /  \     *
 *     \________________/     *
 *                            *
 \****************************/

package com.damienwesterman.defensedrill.ui.view_models;

import androidx.lifecycle.LiveData;

import com.damienwesterman.defensedrill.data.AbstractCategoryEntity;
import com.damienwesterman.defensedrill.ui.utils.CreateNewEntityCallback;

import java.util.List;

// TODO doc comments
public interface AbstractCategoryListViewModel {
    LiveData<List<AbstractCategoryEntity>> getAbstractCategories();
    void populateAbstractCategories();
    void rePopulateAbstractCategories();
    void deleteAbstractCategory();
    void saveAbstractEntity(AbstractCategoryEntity entity, CreateNewEntityCallback callback);
    void updateAbstractEntity(AbstractCategoryEntity entity);
    AbstractCategoryEntity findById(long id);
}
