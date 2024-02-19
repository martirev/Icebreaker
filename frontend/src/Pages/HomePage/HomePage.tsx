import React, { useState } from "react";
import "./HomePage.css";
import Navbar from "../../Components/Navbar/Navbar";
import Sidebar from "../../Components/Sidebar/Sidebar";
import ListView from "../../Components/ListView/ListView";
import LoginModal from "../../Components/LoginModal/LoginModal";
import SignupModal from "../../Components/SignupModal/SignupModal";
import { GameForm } from "../../Components/GameForm/GameForm";
import { categories } from "../../Components/CategoryBox/utils/categories";

export default function HomePage() {
  const [loginModal, setLoginModal] = useState(false);
  const [signupModal, setSignupModal] = useState(false);
  const [formModal, setFormModal] = useState(false);
  const [refreshKey, setRefreshKey] = useState(0);
  /* const [checkedCategories, setCheckedCategories] = useState<Array<boolean>>(
    new Array(categories.length).fill(false)
  ); */

  const toggleLoginModal = () => {
    // localStorage.clear();
    setLoginModal(!loginModal);
  };
  const toggleSignupModal = () => {
    setSignupModal(!signupModal);
  };
  const toggleFormModal = () => {
    setFormModal(!formModal);
  };
  const refreshGameCards = () => {
    setRefreshKey((oldKey) => oldKey + 1);
  };

  return (
    <>
      <Navbar toggleLoginModal={toggleLoginModal}></Navbar>
      <div className="mainBody">
        <Sidebar
          toggleFormModal={toggleFormModal}
          // filterCategories={setCheckedCategories}
        ></Sidebar>
        <ListView
          refreshKey={refreshKey}
          // categoriesToFilter={checkedCategories}
        ></ListView>
      </div>
      <LoginModal
        visibility={loginModal}
        onClose={toggleLoginModal}
        onSignup={() => {
          toggleLoginModal();
          toggleSignupModal();
        }}
      ></LoginModal>
      <SignupModal
        visibility={signupModal}
        onClose={toggleSignupModal}
        onLogin={() => {
          toggleLoginModal();
          toggleSignupModal();
        }}
      ></SignupModal>
      <GameForm
        visibility={formModal}
        onClose={toggleFormModal}
        refreshListView={refreshGameCards}
      ></GameForm>
    </>
  );
}
