package com.ayush.common.transactions

import kotlinx.coroutines.flow.Flow

interface PendingReviewCountSource {
    fun observe(): Flow<Int>
}
