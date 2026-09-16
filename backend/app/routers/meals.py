from fastapi import APIRouter, HTTPException, status, Depends
from sqlalchemy.orm import Session
from datetime import datetime

from app.schemas import MealSwipeRequest, MealSwipeResponse, MealType
from app.db.database import get_db
from app.models.models import User, Card, MealRecord

router = APIRouter(prefix="/meals", tags=["meals"])

@router.post("/swipe", response_model=MealSwipeResponse, status_code=status.HTTP_201_CREATED)
def record_meal(payload: MealSwipeRequest, db: Session = Depends(get_db)) -> MealSwipeResponse:
    # 1. Resolve User from Card
    card = db.query(Card).filter(Card.uid == payload.card_uid).first()
    if not card:
        raise HTTPException(status_code=404, detail="Card not registered")
    
    user = card.user
    if not user.is_active:
         raise HTTPException(status_code=403, detail="User account inactive")

    # 2. Check for duplicate meal today
    timestamp = payload.timestamp or datetime.utcnow()
    start_of_day = timestamp.replace(hour=0, minute=0, second=0, microsecond=0)
    
    existing_meal = db.query(MealRecord).filter(
        MealRecord.user_id == user.id,
        MealRecord.meal_type == payload.meal_type,
        MealRecord.swipe_timestamp >= start_of_day
    ).first()
    
    if existing_meal:
        raise HTTPException(status_code=409, detail=f"Already had {payload.meal_type.value} today")

    # 3. Record Meal
    meal = MealRecord(
        user_id=user.id,
        meal_type=payload.meal_type,
        swipe_timestamp=timestamp
    )
    db.add(meal)
    db.commit()
    db.refresh(meal)
    
    return meal
