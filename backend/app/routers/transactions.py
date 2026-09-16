from fastapi import APIRouter, HTTPException, status, Depends
from sqlalchemy.orm import Session
from datetime import datetime

from app.auth_deps import current_user
from app.permissions import Permission, has_permission
from app.schemas import TransactionRequest, TransactionResponse, TransactionType
from app.db.database import get_db
from app.models.models import User, Card, TransactionRecord

router = APIRouter(prefix="/transactions", tags=["transactions"])

# Which permission each direction of money movement requires. Crediting a wallet and debiting it are
# separate rights: an organizer at the till has CHARGE_TOKENS but not TOP_UP_WALLET.
_REQUIRED_PERMISSION = {
    TransactionType.recharge: Permission.TOP_UP_WALLET,
    TransactionType.deduction: Permission.CHARGE_TOKENS,
}


@router.post("", response_model=TransactionResponse, status_code=status.HTTP_201_CREATED)
def process_transaction(
    payload: TransactionRequest,
    actor: User = Depends(current_user),
    db: Session = Depends(get_db),
) -> TransactionResponse:
    # 0. Authorize the specific direction. Checked before touching any balance.
    needed = _REQUIRED_PERMISSION[payload.transaction_type]
    if not has_permission(actor.role, needed):
        detail = (
            "Only an admin can top up a wallet."
            if payload.transaction_type == TransactionType.recharge
            else "Your role can't take payment."
        )
        raise HTTPException(status_code=status.HTTP_403_FORBIDDEN, detail=detail)

    # 1. Resolve User
    card = db.query(Card).filter(Card.uid == payload.card_uid).first()
    if not card:
        raise HTTPException(status_code=404, detail="Card not registered")
    
    user = card.user
    
    # 2. Process based on type
    if payload.transaction_type == TransactionType.recharge:
        user.token_balance += payload.amount
    elif payload.transaction_type == TransactionType.deduction:
        if user.token_balance < payload.amount:
            raise HTTPException(status_code=400, detail="Insufficient balance")
        user.token_balance -= payload.amount
        
    # 3. Record Transaction
    transaction = TransactionRecord(
        user_id=user.id,
        amount=payload.amount,
        transaction_type=payload.transaction_type,
        description=payload.description,
        created_at=datetime.utcnow()
    )
    
    db.add(transaction)
    db.add(user) # Update user balance
    db.commit()
    db.refresh(transaction)
    
    return transaction
