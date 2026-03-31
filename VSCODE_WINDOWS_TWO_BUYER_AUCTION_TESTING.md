# VS Code Terminal Testing for Windows: Two-Buyer Auction Demo

This guide is for demonstrating competitive bidding from the VS Code integrated terminal on Windows using PowerShell.

It is based on the same flow as `VSCODE_WINDOWS_TERMINAL_TESTING.md`, but this version is specifically for:

- one seller
- two buyers
- both buyers selecting the same item
- both buyers placing bids
- the higher bidder winning after the auction ends

## 1. Open the Project in VS Code

Open the folder:

```text
forward-auction-system-dev
```

Open three VS Code terminals:

- Terminal 1: seller
- Terminal 2: buyer 1
- Terminal 3: buyer 2

You will also use Terminal 1 to start the services first.

## 2. Prerequisites

Check these in Terminal 1:

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

In each terminal, make sure you are at the project root:

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

In Terminal 1:

```powershell
Remove-Item -Force iam\data\*.db,catalogue\data\*.db,auction\data\*.db,payment\data\*.db -ErrorAction SilentlyContinue
```

## 5. Build All Services

In Terminal 1:

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

In Terminal 1:

```powershell
.\run_all.bat
```

Leave Terminal 1 open.

## 7. Verify the Services Are Up

In Terminal 2 or Terminal 3:

```powershell
curl.exe "http://localhost:8080/health"
curl.exe "http://localhost:8080/ping/iam"
curl.exe "http://localhost:8080/ping/catalogue"
curl.exe "http://localhost:8080/ping/auction"
curl.exe "http://localhost:8080/ping/payment"
```

## 8. Create Shared Test Usernames

Run this once in Terminal 1:

```powershell
$RUN_ID = [DateTimeOffset]::UtcNow.ToUnixTimeSeconds()
$SELLER_USER = "seller_$RUN_ID"
$BUYER1_USER = "buyer1_$RUN_ID"
$BUYER2_USER = "buyer2_$RUN_ID"
$SELLER_PASS = "pass1234"
$BUYER1_PASS = "pass1234"
$BUYER2_PASS = "pass1234"
```

Copy these values into Terminal 2 and Terminal 3 as well so all terminals use the same usernames:
```
$RUN_ID
```
1773204877
```powershell
$RUN_ID = 1773205368
$SELLER_USER = "seller_$RUN_ID"
$BUYER1_USER = "buyer1_$RUN_ID"
$BUYER2_USER = "buyer2_$RUN_ID"
$SELLER_PASS = "pass1234"
$BUYER1_PASS = "pass1234"
$BUYER2_PASS = "pass1234"
```

## 9. Seller Signup and Login

Run these in Terminal 1.

### Seller signup

```powershell
$sellerSignupBody = @{
  username = $SELLER_USER
  password = $SELLER_PASS
  firstName = "Alice"
  lastName = "Seller"
  address = "123 Seller St"
} | ConvertTo-Json

Invoke-RestMethod -Method Post `
  -Uri "http://localhost:8080/auth/signup" `
  -ContentType "application/json" `
  -Body $sellerSignupBody
```

### Buyer 1 signup

```powershell
$buyer1SignupBody = @{
  username = $BUYER1_USER
  password = $BUYER1_PASS
  firstName = "Bob"
  lastName = "BuyerOne"
  address = "111 Buyer One Ave"
} | ConvertTo-Json

Invoke-RestMethod -Method Post `
  -Uri "http://localhost:8080/auth/signup" `
  -ContentType "application/json" `
  -Body $buyer1SignupBody
```

### Buyer 2 signup

```powershell
$buyer2SignupBody = @{
  username = $BUYER2_USER
  password = $BUYER2_PASS
  firstName = "Carol"
  lastName = "BuyerTwo"
  address = "222 Buyer Two Ave"
} | ConvertTo-Json

Invoke-RestMethod -Method Post `
  -Uri "http://localhost:8080/auth/signup" `
  -ContentType "application/json" `
  -Body $buyer2SignupBody
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
$SELLER_TOKEN
```

## 10. Seller Creates the Auction Item

Run in Terminal 1:

```powershell
$createItemBody = @{
  title = "Competitive Auction Laptop"
  description = "Auction demo item for two buyers"
  condition = "NEW"
  keywords = "laptop auction demo"
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

Copy the resulting `ITEM_ID` into Terminal 2 and Terminal 3:

```powershell
$ITEM_ID = <item_id_from_terminal_1>
$ITEM_ID = 1
```

## 11. Buyer 1 Login and Select the Item

Run in Terminal 2.

### Buyer 1 login

```powershell
$buyer1LoginBody = @{
  username = $BUYER1_USER
  password = $BUYER1_PASS
} | ConvertTo-Json

$buyer1LoginResponse = Invoke-RestMethod -Method Post `
  -Uri "http://localhost:8080/auth/login" `
  -ContentType "application/json" `
  -Body $buyer1LoginBody

$BUYER1_TOKEN = $buyer1LoginResponse.token
$BUYER1_TOKEN
```

### Buyer 1 views item

```powershell
Invoke-RestMethod -Method Get `
  -Uri "http://localhost:8080/catalogue/items/$ITEM_ID"
```

### Buyer 1 selects the item

```powershell
Invoke-RestMethod -Method Post `
  -Uri "http://localhost:8080/catalogue/select/$ITEM_ID" `
  -Headers @{ Authorization = "Bearer $BUYER1_TOKEN" }
```

## 12. Buyer 2 Login and Select the Same Item

Run in Terminal 3.

### Buyer 2 login

```powershell
$buyer2LoginBody = @{
  username = $BUYER2_USER
  password = $BUYER2_PASS
} | ConvertTo-Json

$buyer2LoginResponse = Invoke-RestMethod -Method Post `
  -Uri "http://localhost:8080/auth/login" `
  -ContentType "application/json" `
  -Body $buyer2LoginBody

$BUYER2_TOKEN = $buyer2LoginResponse.token
$BUYER2_TOKEN
```

### Buyer 2 views item

```powershell
Invoke-RestMethod -Method Get `
  -Uri "http://localhost:8080/catalogue/items/$ITEM_ID"
```

### Buyer 2 selects the same item

```powershell
Invoke-RestMethod -Method Post `
  -Uri "http://localhost:8080/catalogue/select/$ITEM_ID" `
  -Headers @{ Authorization = "Bearer $BUYER2_TOKEN" }
```

## 13. Competitive Bidding

Now both buyers are ready to bid on the same item.

Run the following commands in alternating order between Terminal 2 and Terminal 3.

### Buyer 1 places first bid

Run in Terminal 2:

```powershell
$buyer1Bid1Body = @{
  itemId = $ITEM_ID
  amount = 550
} | ConvertTo-Json

Invoke-RestMethod -Method Post `
  -Uri "http://localhost:8080/bid" `
  -ContentType "application/json" `
  -Headers @{ Authorization = "Bearer $BUYER1_TOKEN" } `
  -Body $buyer1Bid1Body
```

### Buyer 2 outbids Buyer 1

Run in Terminal 3:

```powershell
$buyer2Bid1Body = @{
  itemId = $ITEM_ID
  amount = 600
} | ConvertTo-Json

Invoke-RestMethod -Method Post `
  -Uri "http://localhost:8080/bid" `
  -ContentType "application/json" `
  -Headers @{ Authorization = "Bearer $BUYER2_TOKEN" } `
  -Body $buyer2Bid1Body
```

### Buyer 1 bids again

Run in Terminal 2:

```powershell
$buyer1Bid2Body = @{
  itemId = $ITEM_ID
  amount = 650
} | ConvertTo-Json

Invoke-RestMethod -Method Post `
  -Uri "http://localhost:8080/bid" `
  -ContentType "application/json" `
  -Headers @{ Authorization = "Bearer $BUYER1_TOKEN" } `
  -Body $buyer1Bid2Body
```

### Buyer 2 places the highest bid

Run in Terminal 3:

```powershell
$buyer2Bid2Body = @{
  itemId = $ITEM_ID
  amount = 700
} | ConvertTo-Json

Invoke-RestMethod -Method Post `
  -Uri "http://localhost:8080/bid" `
  -ContentType "application/json" `
  -Headers @{ Authorization = "Bearer $BUYER2_TOKEN" } `
  -Body $buyer2Bid2Body
```

## 14. Check Bid History

Run in any terminal:

```powershell
Invoke-RestMethod -Method Get `
  -Uri "http://localhost:8080/bid/history/$ITEM_ID"
```

You should see bids from both buyers, and the highest amount should be `700`.

## 15. Wait for Auction End

Because the auction duration is `45` seconds, wait long enough for it to expire:

```powershell
Start-Sleep -Seconds 50
```

## 16. Confirm the Winner

### Buyer 1 checks auction result

Run in Terminal 2:

```powershell
Invoke-RestMethod -Method Get `
  -Uri "http://localhost:8080/auction-result/$ITEM_ID" `
  -Headers @{ Authorization = "Bearer $BUYER1_TOKEN" }
```

Expected:

- `status = ENDED`
- `result = SOLD`
- `winner = buyer2_<RUN_ID>`
- `isWinner = False`

### Buyer 2 checks auction result

Run in Terminal 3:

```powershell
Invoke-RestMethod -Method Get `
  -Uri "http://localhost:8080/auction-result/$ITEM_ID" `
  -Headers @{ Authorization = "Bearer $BUYER2_TOKEN" }
```

Expected:

- `status = ENDED`
- `result = SOLD`
- `winner = buyer2_<RUN_ID>`
- `isWinner = True`

## 17. Winner Payment Flow

Only the winning buyer should be able to continue to payment.

### Buyer 2 opens the payment page

Run in Terminal 3:

```powershell
Invoke-RestMethod -Method Get `
  -Uri "http://localhost:8080/payment-page/$ITEM_ID" `
  -Headers @{ Authorization = "Bearer $BUYER2_TOKEN" }
```

### Buyer 2 submits payment

Run in Terminal 3:

```powershell
$paymentBody = @{
  itemId = $ITEM_ID
  expedited = $true
  cardName = "Carol BuyerTwo"
  cardNumber = "4111111111111111"
  expiryDate = "12/28"
  securityCode = "123"
} | ConvertTo-Json

$paymentResponse = Invoke-RestMethod -Method Post `
  -Uri "http://localhost:8080/pay" `
  -ContentType "application/json" `
  -Headers @{ Authorization = "Bearer $BUYER2_TOKEN" } `
  -Body $paymentBody

$PAYMENT_ID = $paymentResponse.paymentId
$paymentResponse
$PAYMENT_ID
```

### Buyer 2 views receipt

Run in Terminal 3:

```powershell
Invoke-RestMethod -Method Get `
  -Uri "http://localhost:8080/receipt/$PAYMENT_ID" `
  -Headers @{ Authorization = "Bearer $BUYER2_TOKEN" }
```

## 18. Optional Losing-Buyer Check

Buyer 1 should not be able to access the payment page for the item after losing.

Run in Terminal 2:

```powershell
Invoke-RestMethod -Method Get `
  -Uri "http://localhost:8080/payment-page/$ITEM_ID" `
  -Headers @{ Authorization = "Bearer $BUYER1_TOKEN" }
```

Expected:

- request should fail
- only the winner can access payment

## 19. What This Demonstrates

This flow demonstrates:

- multiple buyers can target the same item
- both buyers can independently select that same item in their own sessions
- bids are tracked on one auction
- the highest bidder at auction end becomes the winner
- only the winner can proceed to payment and receipt

## 20. Common Mistakes

- Do not use the same buyer token in both buyer terminals.
- Make sure both buyers set the exact same `ITEM_ID`.
- Do not wait too long before selection and bidding, or the auction may end early.
- Do not use Bash syntax in PowerShell.
- Use `.\run_all.bat`, not `run_all.bat`.

## 21. Recommended Demo Order

1. Start services in Terminal 1.
2. Create seller, buyer 1, and buyer 2.
3. Seller creates one auction item.
4. Buyer 1 selects the item in Terminal 2.
5. Buyer 2 selects the same item in Terminal 3.
6. Alternate bids between the two buyer terminals.
7. Check bid history.
8. Wait for auction end.
9. Show that the higher bidder wins.
10. Show that only the winner can pay.
