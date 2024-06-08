//package com.chs.yourapphistory.data.paging
//
//import androidx.paging.PagingSource
//import androidx.paging.PagingState
//import com.chs.yourapphistory.common.Constants
//import com.chs.yourapphistory.common.reverseDateUntil
//import com.chs.yourapphistory.data.db.dao.AppInfoDao
//import com.chs.yourapphistory.domain.model.AppDetailInfo
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.withContext
//import java.time.LocalDate
//
//class GetPagingAppDetailList(
//    private val appInfo: AppInfoDao
//) : PagingSource<LocalDate, Pair<LocalDate, List<AppDetailInfo>>>() {
//    override fun getRefreshKey(state: PagingState<LocalDate, Pair<LocalDate, List<AppDetailInfo>>>): LocalDate? {
//        return state.anchorPosition?.let { position ->
//            val page = state.closestPageToPosition(position)
//            page?.prevKey?.minusDays(1) ?: page?.nextKey?.plusDays(1)
//        }
//    }
//
//    override suspend fun load(params: LoadParams<LocalDate>): LoadResult<LocalDate, Pair<LocalDate, List<AppDetailInfo>>> {
//        val pageDate: LocalDate = params.key ?: LocalDate.now()
//        pageDate.run { this.minusDays(Constants.PAGING_DAY) }
//            .reverseDateUntil(pageDate.plusDays(1L))
//            .map {
//                withContext(Dispatchers.IO) {
//
//                }
//
//            }
//    }
//}