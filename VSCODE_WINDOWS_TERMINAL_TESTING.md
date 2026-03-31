# VS Code Terminal Testing for Windows

This guide is for running and testing the project from the VS Code integrated terminal on Windows.

It assumes:

- you are using the VS Code terminal
- your shell is PowerShell
- you want terminal commands only
- you want to test each use case manually through the gateway

## 1. Open the Project in VS Code

Open the folder:

```text
forward-auction-system-dev
```

Then open a new VS Code terminal.

Verify you are in PowerShell:

```powershell
$PSVersionTable.PSVersion
```

## 2. Prerequisites

Make sure these are available:

```powershell
java -version
curl.exe --version
python --version
```

If `python` is not available, try:

```powershell
python3 --version
```

## 3. Go to the Project Root

In the VS Code terminal, make sure you are at the project root:

```powershell
Get-Location
Get-ChildItem
```

You should see folders like:

- `iam`
- `catalogue`
- `auction`
- `payment`
- `gateway`
- `scripts`

## 4. Clean Old Local Databases

Run:

```powershell
Remove-Item -Force iam\data\*.db,catalogue\data\*.db,auction\data\*.db,payment\data\*.db -ErrorAction SilentlyContinue
```

## 5. Build All Services

Run this from the project root:

```powershell
$services = 'iam','catalogue','auction','payment','gateway'
foreach ($s in $services) {
  Push-Location $s
  try {
    .\mvnw.cmd clean package -DskipTests
    if ($LASTEXITCODE -ne 0) { throw "Build failed in $s" }
  } finally {
    Pop-Location
  }
}
```

## 6. Start All Services

Run:

```powershell
.\run_all.bat
```

Leave that terminal open.

Open a second VS Code terminal for testing requests.

## 7. Verify the Services Are Up

In the second terminal, run:

```powershell
curl.exe "http://localhost:8080/health"
curl.exe "http://localhost:8080/ping/iam"
curl.exe "http://localhost:8080/ping/catalogue"
curl.exe "http://localhost:8080/ping/auction"
curl.exe "http://localhost:8080/ping/payment"
```

## 8. Set Reusable Test Variables

Run this once in the second terminal:

```powershell
$RUN_ID = [int][double]::Parse((Get-Date -UFormat %s))
$SELLER_USER = "seller_$RUN_ID"
$BUYER_USER = "buyer_$RUN_ID"
$SELLER_PASS = "pass1234"
$BUYER_PASS = "pass1234"
```

If `Get-Date -UFormat %s` does not work in your PowerShell version, use:

```powershell
$RUN_ID = [DateTimeOffset]::UtcNow.ToUnixTimeSeconds()
```

## 9. UC1: User Registration and Login

### Seller signup

```powershell
$sellerSignupBody = @{
  username = $SELLER_USER
  password = $SELLER_PASS
  firstName = "Alice"
  lastName = "Smith"
  address = "123 Seller St"
} | ConvertTo-Json

Invoke-RestMethod -Method Post `
  -Uri "http://localhost:8080/auth/signup" `
  -ContentType "application/json" `
  -Body $sellerSignupBody
```

### Buyer signup

```powershell
$buyerSignupBody = @{
  username = $BUYER_USER
  password = $BUYER_PASS
  firstName = "Bob"
  lastName = "Jones"
  address = "456 Buyer Ave"
} | ConvertTo-Json

Invoke-RestMethod -Method Post `
  -Uri "http://localhost:8080/auth/signup" `
  -ContentType "application/json" `
  -Body $buyerSignupBody
```

### Seller login

```powershell
$sellerLoginBody = @{
  username = $SELLER_USER
  password = $SELLER_PASS
} | ConvertTo-Json

$sellerLoginResponse = Invoke-RestMethod -Method Post `
  -Uri "http://localhost:8080/auth/login" `
  -ContentType "application/json" `
  -Body $sellerLoginBody

$SELLER_TOKEN = $sellerLoginResponse.token
$sellerLoginResponse
$SELLER_TOKEN
```

### Buyer login

```powershell
$buyerLoginBody = @{
  username = $BUYER_USER
  password = $BUYER_PASS
} | ConvertTo-Json

$buyerLoginResponse = Invoke-RestMethod -Method Post `
  -Uri "http://localhost:8080/auth/login" `
  -ContentType "application/json" `
  -Body $buyerLoginBody

$BUYER_TOKEN = $buyerLoginResponse.token
$buyerLoginResponse
$BUYER_TOKEN
```

### Check current session

```powershell
Invoke-RestMethod -Method Get `
  -Uri "http://localhost:8080/auth/me" `
  -Headers @{ Authorization = "Bearer $SELLER_TOKEN" }
```

### Reset password

```powershell
$resetBody = @{
  username = $SELLER_USER
  currentPassword = $SELLER_PASS
  newPassword = "newpass123"
} | ConvertTo-Json

Invoke-RestMethod -Method Post `
  -Uri "http://localhost:8080/auth/reset-password" `
  -ContentType "application/json" `
  -Headers @{ Authorization = "Bearer $SELLER_TOKEN" } `
  -Body $resetBody
```

### Logout

```powershell
Invoke-RestMethod -Method Post `
  -Uri "http://localhost:8080/auth/logout" `
  -Headers @{ Authorization = "Bearer $SELLER_TOKEN" }
```

## 10. UC7: Create Item and Auction

Prerequisite:

- seller is logged in
- `SELLER_TOKEN` exists

```powershell
$sellerLoginBody = @{
  username = $SELLER_USER
  password = "newpass123"
} | ConvertTo-Json

$sellerLoginResponse = Invoke-RestMethod -Method Post `
  -Uri "http://localhost:8080/auth/login" `
  -ContentType "application/json" `
  -Body $sellerLoginBody

$SELLER_TOKEN = $sellerLoginResponse.token
$SELLER_TOKEN
```

```powershell
$createItemBody = @{
  title = "test 2 Gaming Laptop"
  description = "RTX 4090 laptop"
  condition = "NEW"
  keywords = "laptop gaming"
  shippingCost = 15.0
  expeditedShippingCost = 10.0
  shippingDays = 5
  startingPrice = 500.0
  auctionDurationSeconds = 120
} | ConvertTo-Json

$createItemResponse = Invoke-RestMethod -Method Post `
  -Uri "http://localhost:8080/items" `
  -ContentType "application/json" `
  -Headers @{ Authorization = "Bearer $SELLER_TOKEN" } `
  -Body $createItemBody

$ITEM_ID = $createItemResponse.itemId
$createItemResponse
$ITEM_ID
```

## 11. UC2: Browse, Search, View, and Select

### Browse all items

```powershell
Invoke-RestMethod -Method Get `
  -Uri "http://localhost:8080/catalogue"
```

### Search items

```powershell
Invoke-RestMethod -Method Get `
  -Uri "http://localhost:8080/catalogue?keyword=laptop"
```

### View one item

```powershell
Invoke-RestMethod -Method Get `
  -Uri "http://localhost:8080/catalogue/items/$ITEM_ID"
```

### Select item for bidding

Select item id that you want to bid for 
Susbtitute $ITEM_ID for the item you want to bid for 

```powershell
Invoke-RestMethod -Method Post `
  -Uri "http://localhost:8080/catalogue/select/$ITEM_ID" `
  -Headers @{ Authorization = "Bearer $BUYER_TOKEN" }
```



## 12. UC3: Place Bid and View Bid History

Prerequisite:

- buyer is logged in
- item was selected first

### Place a bid

```powershell
$bidBody = @{
  itemId = $ITEM_ID
  amount = 600
} | ConvertTo-Json

Invoke-RestMethod -Method Post `
  -Uri "http://localhost:8080/bid" `
  -ContentType "application/json" `
  -Headers @{ Authorization = "Bearer $BUYER_TOKEN" } `
  -Body $bidBody
```

### View bid history

```powershell
Invoke-RestMethod -Method Get `
  -Uri "http://localhost:8080/bid/history/$ITEM_ID"
```

## 13. UC4: View Auction Result

Wait for the auction to expire:

```powershell
Start-Sleep -Seconds 20
```

Then check result:

```powershell
Invoke-RestMethod -Method Get `
  -Uri "http://localhost:8080/auction-result/$ITEM_ID" `
  -Headers @{ Authorization = "Bearer $BUYER_TOKEN" }
```

## 14. UC5: Payment

### View payment page

```powershell
Invoke-RestMethod -Method Get `
  -Uri "http://localhost:8080/payment-page/$ITEM_ID" `
  -Headers @{ Authorization = "Bearer $BUYER_TOKEN" }
```

### Submit payment

```powershell
$paymentBody = @{
  itemId = $ITEM_ID
  expedited = $true
  cardName = "Bob Jones"
  cardNumber = "4111111111111111"
  expiryDate = "12/28"
  securityCode = "123"
} | ConvertTo-Json

$paymentResponse = Invoke-RestMethod -Method Post `
  -Uri "http://localhost:8080/pay" `
  -ContentType "application/json" `
  -Headers @{ Authorization = "Bearer $BUYER_TOKEN" } `
  -Body $paymentBody

$PAYMENT_ID = $paymentResponse.paymentId
$paymentResponse
$PAYMENT_ID
```

## 15. UC6: Receipt

```powershell
Invoke-RestMethod -Method Get `
  -Uri "http://localhost:8080/receipt/$PAYMENT_ID" `
  -Headers @{ Authorization = "Bearer $BUYER_TOKEN" }
```

## 16. Existing Repo Scripts

If you want the existing scripted flows:

- `.\run_all.bat`
- `bash scripts/main_flow/00_smoke.sh`
- `bash scripts/main_flow/full_flow.sh`
- `bash scripts/robustness/test_robustness.sh`
- `bash scripts/manual_curl_use_cases.sh`

## 17. Common Mistakes in VS Code Terminal on Windows

- Do not run local files as `run_all.bat`; use `.\run_all.bat`.
- Do not paste Bash `curl` commands with `\` line continuations into PowerShell.
- In PowerShell, `curl` may map to `Invoke-WebRequest`; use `curl.exe` if you need actual curl.
- `Invoke-RestMethod` is the safest option for JSON APIs in PowerShell.
- If startup fails, check the terminal running `.\run_all.bat` for service errors.

## 18. Recommended Flow

1. Open one VS Code terminal.
2. Clean old DB files.
3. Build all services.
4. Start services with `.\run_all.bat`.
5. Open a second VS Code terminal.
6. Run health checks.
7. Run UC1 seller and buyer signup/login.
8. Run UC7 create item.
9. Run UC2 browse and select.
10. Run UC3 bid.
11. Wait for auction end.
12. Run UC4 result.
13. Run UC5 payment.
14. Run UC6 receipt.
