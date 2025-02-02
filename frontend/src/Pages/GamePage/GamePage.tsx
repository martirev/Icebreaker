import React, { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import Navbar from "../../Components/Navbar/Navbar";
import "./GamePage.css";
import "./DarkGamePage.css";
import { GameDetails } from "../../Components/GameDetails/GameDetails";
import LoginModal from "../../Components/LoginModal/LoginModal";
import SignupModal from "../../Components/SignupModal/SignupModal";
import { useDarkMode } from "../../Contexts/DarkModeContext";

type RouteParams = {
  gameId: string;
};
type Game = {
  id: string;
  title: string;
  categories: string[];
  description: string;
  rules: string;
  averageRating?: number;
};
const gameCardApiUrl = "http://localhost:8080/api/gamecard";

const GamePage = () => {
  const { gameId } = useParams<RouteParams>();
  const [game, setGame] = useState<Game | null>(null);
  const [loginModal, setLoginModal] = useState(false);
  const [signupModal, setSignupModal] = useState(false);
  const {isDarkMode} = useDarkMode()

  useEffect(() => {
    if (gameId) {
      fetch(`${gameCardApiUrl}/get/id/${gameId}`, {
        method: "GET",
      })

        .then((response) =>
          response.json().then((data) => {
            if (!response.ok) {
              throw new Error(data.message);
            }
            setGame({
              id: gameId,
              title: data.title,
              categories: data.categories || [],
              description: data.description,
              rules: data.rules,
              averageRating: data.averageRating,
            });
          })
        )
        .catch((error) => {
          console.error("Error fetching game card:", error);
          alert(error);
        });
    }
  }, [gameId]);

  if (!gameId) {
    return <div>Loading...</div>;
  }

  if (!game) {
    return <div>Game is null</div>;
  }

  const toggleLoginModal = () => {
    // localStorage.clear();
    setLoginModal(!loginModal);
  };
  const toggleSignupModal = () => {
    setSignupModal(!signupModal);
  };

  return (
    <>
      <div className={`mainGameBody ${isDarkMode ? "dark" : ""}`}>
      <Link to={"/"}>
        <button className="backButton">Hjem</button>
      </Link>
      <Navbar toggleLoginModal={toggleLoginModal}></Navbar>
      </div>
      <GameDetails game={game}></GameDetails>
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
    </>
  );
};

export default GamePage;
