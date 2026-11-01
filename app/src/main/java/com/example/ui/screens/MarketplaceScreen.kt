package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.NovaChatViewModel
import com.example.model.MarketProduct
import com.example.ui.theme.*

@Composable
fun MarketplaceScreen(
    viewModel: NovaChatViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val products = viewModel.marketplaceProducts
    var selectedCategory by remember { mutableStateOf("All") }
    val categories = listOf("All", "Textiles", "Beauty", "Art", "Furniture")

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(NovaDarkBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 14.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("market_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = NovaTextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = "NOVA MARKETPLACE",
                            color = NovaTextPrimary,
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "Verified African Artisans & Makers",
                            color = NovaCyanAccent,
                            fontSize = 11.sp
                        )
                    }
                }

                IconButton(
                    onClick = {},
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color(0x3300E5BE))
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = "Cart",
                        tint = NovaCyanAccent,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Categories
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { cat ->
                    val isSelected = cat == selectedCategory
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { selectedCategory = cat },
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) NovaCyanAccent else Color(0x330E1F17),
                        border = BorderStroke(1.dp, if (isSelected) NovaCyanAccent else Color(0x2E00E5BE))
                    ) {
                        Text(
                            text = cat,
                            color = if (isSelected) Color(0xFF04140D) else NovaTextSecondary,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Products Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(products, key = { it.id }) { product ->
                    ProductCard(
                        product = product,
                        onBuy = {
                            viewModel.topUpWallet(-product.priceGhs)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ProductCard(
    product: MarketProduct,
    onBuy: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xCC0E1A15),
        border = BorderStroke(1.dp, Color(0x2600E5BE))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF162B21)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (product.category) {
                        "Textiles" -> Icons.Default.DryCleaning
                        "Beauty" -> Icons.Default.Spa
                        "Art" -> Icons.Default.Palette
                        else -> Icons.Default.Weekend
                    },
                    contentDescription = product.title,
                    tint = NovaCyanAccent,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = product.title,
                color = NovaTextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "${product.merchantName} • ${product.originRegion}",
                color = NovaTextMuted,
                fontSize = 10.sp,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "GH₵ ${product.priceGhs.toInt()}",
                    color = NovaCyanAccent,
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp
                )

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFFFCC00),
                    modifier = Modifier.clickable(onClick = onBuy)
                ) {
                    Text(
                        text = "MoMo",
                        color = Color(0xFF003366),
                        fontWeight = FontWeight.Black,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}
