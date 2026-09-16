from fastapi import APIRouter, HTTPException, status, Depends
from sqlalchemy.orm import Session
from typing import List

from app.schemas import CardLinkRequest, CardResponse, UserCreate, UserResponse, UserUpdate
from app.auth_deps import require
from app.db.database import get_db
from app.models.models import User, Card
from app.permissions import Permission

router = APIRouter(prefix="/users", tags=["users"])

@router.get("", response_model=List[UserResponse])
def list_users(db: Session = Depends(get_db)) -> List[UserResponse]:
    return db.query(User).all()


@router.post("", response_model=UserResponse, status_code=status.HTTP_201_CREATED)
def create_user(
    payload: UserCreate,
    _: User = Depends(require(Permission.MANAGE_USERS)),
    db: Session = Depends(get_db),
) -> UserResponse:
    user = User(
        name=payload.name,
        email=payload.email,
        phone=payload.phone,
        token_balance=payload.token_balance,
        role=payload.role.value
    )
    db.add(user)
    db.commit()
    db.refresh(user)
    return user


@router.get("/{user_id}", response_model=UserResponse)
def get_user(user_id: str, db: Session = Depends(get_db)) -> UserResponse:
    user = db.query(User).filter(User.id == user_id).first()
    if not user:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="User not found")
    return user


@router.put("/{user_id}", response_model=UserResponse)
def update_user(
    user_id: str,
    payload: UserUpdate,
    _: User = Depends(require(Permission.MANAGE_USERS)),
    db: Session = Depends(get_db),
) -> UserResponse:
    user = db.query(User).filter(User.id == user_id).first()
    if not user:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="User not found")
    
    update_data = payload.model_dump(exclude_unset=True)
    for key, value in update_data.items():
        setattr(user, key, value)
        
    db.commit()
    db.refresh(user)
    return user


@router.delete("/{user_id}", status_code=status.HTTP_204_NO_CONTENT)
def delete_user(
    user_id: str,
    _: User = Depends(require(Permission.MANAGE_USERS)),
    db: Session = Depends(get_db),
) -> None:
    user = db.query(User).filter(User.id == user_id).first()
    if user:
        db.delete(user)
        db.commit()
    return None


@router.post("/cards/link", response_model=CardResponse, status_code=status.HTTP_201_CREATED)
def link_card(
    payload: CardLinkRequest,
    _: User = Depends(require(Permission.MANAGE_USERS)),
    db: Session = Depends(get_db),
) -> CardResponse:
    user = db.query(User).filter(User.id == payload.user_id).first()
    if not user:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="User not found")
        
    # Check if card is already linked
    existing_card = db.query(Card).filter(Card.uid == payload.card_uid).first()
    if existing_card:
         # Re-link logic if needed, for now fail
         raise HTTPException(status_code=400, detail="Card already linked")

    card = Card(uid=payload.card_uid, user_id=user.id)
    db.add(card)
    db.commit()
    return CardResponse(card_uid=payload.card_uid, user=user)


@router.get("/cards/{uid}", response_model=CardResponse)
def get_card(uid: str, db: Session = Depends(get_db)) -> CardResponse:
    card = db.query(Card).filter(Card.uid == uid).first()
    if not card:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Card not linked")
    return CardResponse(card_uid=card.uid, user=card.user)
