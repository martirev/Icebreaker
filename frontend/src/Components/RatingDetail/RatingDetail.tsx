import "./RatingDetail.css";

type RatingDetailProps = {
  rating: {
    score: number;
    comment: string;
    username: string;
  };
};

export function RatingDetail({ rating }: RatingDetailProps) {
  const { score, comment, username } = rating;

  return (
    <div className="container">
      <div className="commentHeader">
        <p className="username">{username}</p>
        <p className="ratingScore">Rating:
          <span
            style={{
              marginLeft: '5px',
              fontSize: '20px',
              paddingTop: '10px',
              color: 'gold'
            }}
          >
            &#9733; {/* Stjerne */}
          </span> {score}</p>
      </div>
      <p className="comment">{comment}</p>
    </div>
  );
}
